package com.example.booknest3

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.DecimalFormat
import kotlin.concurrent.thread

class BookDetailsFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var bookId: String? = null
    private var isBookmarked = false
    private lateinit var bookmarkButton: MaterialButton
    private lateinit var toolbar: Toolbar
    private var downloadMenuItem: MenuItem? = null

    // Views for Ratings and Reviews
    private lateinit var reviewsRecyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private val reviewsList = mutableListOf<Review>()
    private lateinit var averageRatingValue: TextView
    private lateinit var averageRatingStars: RatingBar
    private lateinit var totalRatingsCount: TextView
    private lateinit var progress5Stars: ProgressBar
    private lateinit var progress4Stars: ProgressBar
    private lateinit var progress3Stars: ProgressBar
    private lateinit var progress2Stars: ProgressBar
    private lateinit var progress1Star: ProgressBar
    private lateinit var userRatingBar: RatingBar
    private lateinit var writeReviewLink: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bookId = arguments?.getString("BOOK_ID")
        return inflater.inflate(R.layout.book_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize existing views
        val startReadingButton = view.findViewById<MaterialButton>(R.id.start_reading_button)
        val backArrowButton = view.findViewById<ImageView>(R.id.back_arrow)
        bookmarkButton = view.findViewById(R.id.bookmark_button)
        toolbar = view.findViewById(R.id.toolbar)

        // Initialize ratings and reviews views
        initReviewsViews(view)

        setupToolbar()
        setupReviewsRecyclerView()
        setupWriteReviewIntent()

        bookId?.let {
            loadBookDetails(it, view)
            checkIfBookmarked(it)
            checkIfBookIsDownloaded(it)
            loadRatingsAndReviews(it)
        }

        startReadingButton.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("BOOK_ID", bookId)
            val readBookFragment = ReadBookFragment()
            readBookFragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, readBookFragment)
                .addToBackStack(null)
                .commit()
        }

        backArrowButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        bookmarkButton.setOnClickListener {
            bookId?.let { id -> toggleBookmarkState(id) }
        }
    }

    private fun initReviewsViews(view: View) {
        reviewsRecyclerView = view.findViewById(R.id.reviews_recycler_view)
        averageRatingValue = view.findViewById(R.id.average_rating_value)
        averageRatingStars = view.findViewById(R.id.average_rating_stars)
        totalRatingsCount = view.findViewById(R.id.total_ratings_count)
        progress5Stars = view.findViewById(R.id.progress_5_stars)
        progress4Stars = view.findViewById(R.id.progress_4_stars)
        progress3Stars = view.findViewById(R.id.progress_3_stars)
        progress2Stars = view.findViewById(R.id.progress_2_stars)
        progress1Star = view.findViewById(R.id.progress_1_star)
        userRatingBar = view.findViewById(R.id.user_rating_bar)
        writeReviewLink = view.findViewById(R.id.write_review_link)
    }

    private fun setupWriteReviewIntent() {
        val openReviewActivity = { ->
            val intent = Intent(requireActivity(), WriteReviewActivity::class.java)
            intent.putExtra("BOOK_ID", bookId)
            startActivity(intent)
        }

        writeReviewLink.setOnClickListener {
            openReviewActivity()
        }

        userRatingBar.setOnRatingBarChangeListener { _, _, _ ->
            openReviewActivity()
        }
    }

    private fun setupReviewsRecyclerView() {
        reviewAdapter = ReviewAdapter(reviewsList)
        reviewsRecyclerView.adapter = reviewAdapter
        reviewsRecyclerView.layoutManager = LinearLayoutManager(context)
        reviewsRecyclerView.isNestedScrollingEnabled = false
    }

    private fun loadRatingsAndReviews(bookId: String) {
        db.collection("books").document(bookId).collection("reviews")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20) // Get the latest 20 reviews
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    // You can show a "No reviews yet" message here if you want
                    return@addOnSuccessListener
                }
                reviewsList.clear()
                for (doc in snapshot.documents) {
                    val review = doc.toObject(Review::class.java)
                    if (review != null) {
                        reviewsList.add(review)
                    }
                }
                reviewAdapter.notifyDataSetChanged()
                updateRatingsSummaryUI(reviewsList)
            }
            .addOnFailureListener { e ->
                Log.e("BookDetails", "Error loading reviews", e)
            }
    }

    private fun updateRatingsSummaryUI(reviews: List<Review>) {
        if (reviews.isEmpty()) return

        val totalReviews = reviews.size
        val average = reviews.sumOf { it.rating.toDouble() } / totalReviews
        val ratingCounts = IntArray(5)
        for (review in reviews) {
            val ratingIndex = review.rating.toInt() - 1
            if (ratingIndex in 0..4) {
                ratingCounts[ratingIndex]++
            }
        }

        val decimalFormat = DecimalFormat("#.##")
        averageRatingValue.text = decimalFormat.format(average)
        averageRatingStars.rating = average.toFloat()
        totalRatingsCount.text = totalReviews.toString()

        progress5Stars.progress = if (totalReviews > 0) (ratingCounts[4] * 100) / totalReviews else 0
        progress4Stars.progress = if (totalReviews > 0) (ratingCounts[3] * 100) / totalReviews else 0
        progress3Stars.progress = if (totalReviews > 0) (ratingCounts[2] * 100) / totalReviews else 0
        progress2Stars.progress = if (totalReviews > 0) (ratingCounts[1] * 100) / totalReviews else 0
        progress1Star.progress = if (totalReviews > 0) (ratingCounts[0] * 100) / totalReviews else 0
    }

    private fun setupToolbar() {
        downloadMenuItem = toolbar.menu.findItem(R.id.action_download)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_download -> {
                    bookId?.let { downloadBook(it) }
                    true
                }
                R.id.action_more -> {
                    Toast.makeText(context, "More options clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun checkIfBookIsDownloaded(bookId: String) {
        val downloadDir = File(requireContext().filesDir, "downloads")
        val bookFile = File(downloadDir, "$bookId.json")
        if (bookFile.exists()) {
            downloadMenuItem?.isEnabled = false
            downloadMenuItem?.icon?.alpha = 130 // Make it look disabled
        }
    }

    private fun downloadBook(bookId: String) {
        downloadMenuItem?.isEnabled = false // Disable button immediately
        view?.let { Snackbar.make(it, "Downloading book...", Snackbar.LENGTH_LONG).show() }

        val bookDetails = mutableMapOf<String, Any?>()
        val pagesList = mutableListOf<Map<String, Any?>>()

        db.collection("books").document(bookId).get()
            .addOnSuccessListener { bookDoc ->
                if (!bookDoc.exists()) {
                    view?.let { Snackbar.make(it, "Error: Book not found.", Snackbar.LENGTH_LONG).show() }
                    downloadMenuItem?.isEnabled = true
                    return@addOnSuccessListener
                }
                val currentBook = bookDoc.toObject(Book::class.java)
                bookDetails["title"] = currentBook?.title
                bookDetails["author"] = currentBook?.author
                bookDetails["description"] = currentBook?.description
                bookDetails["coverImageUrl"] = currentBook?.coverImageUrl

                // Now fetch pages
                db.collection("books").document(bookId).collection("pages")
                    .orderBy("pageNumber", Query.Direction.ASCENDING).get()
                    .addOnSuccessListener { pagesSnapshot ->
                        for (pageDoc in pagesSnapshot.documents) {
                            pagesList.add(pageDoc.data ?: emptyMap())
                        }
                        thread { // Run file writing on a background thread
                            saveBookToFile(bookId, bookDetails, pagesList)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Download", "Error fetching pages", e)
                        view?.let { Snackbar.make(it, "Error: Could not fetch pages.", Snackbar.LENGTH_LONG).show() }
                        downloadMenuItem?.isEnabled = true
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Download", "Error fetching book details", e)
                view?.let { Snackbar.make(it, "Error: Could not fetch book details.", Snackbar.LENGTH_LONG).show() }
                downloadMenuItem?.isEnabled = true
            }
    }

    private fun saveBookToFile(bookId: String, bookDetails: Map<String, Any?>, pages: List<Map<String, Any?>>) {
        try {
            val jsonObject = JSONObject()
            bookDetails.forEach { (key, value) -> jsonObject.put(key, value) }
            jsonObject.put("pages", JSONArray(pages))

            val downloadDir = File(requireContext().filesDir, "downloads")
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }

            val bookFile = File(downloadDir, "$bookId.json")
            bookFile.writeText(jsonObject.toString(4)) // Use indentation for readability

            activity?.runOnUiThread {
                view?.let { Snackbar.make(it, "Download complete!", Snackbar.LENGTH_LONG).show() }
                downloadMenuItem?.icon?.alpha = 130 // Visually confirm disabled state
            }
        } catch (e: Exception) {
            Log.e("Download", "Failed to save book file", e)
            activity?.runOnUiThread {
                view?.let { Snackbar.make(it, "Error: Failed to save file.", Snackbar.LENGTH_LONG).show() }
                downloadMenuItem?.isEnabled = true // Re-enable on failure
            }
        }
    }

    private fun checkIfBookmarked(bookId: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            bookmarkButton.isEnabled = false
            return
        }
        db.collection("users").document(userId).collection("bookmarks").document(bookId)
            .get()
            .addOnSuccessListener { document ->
                isBookmarked = document.exists()
                updateBookmarkButtonState()
            }
    }

    private fun toggleBookmarkState(bookId: String) {
        val userId = auth.currentUser?.uid ?: return
        val bookmarkRef = db.collection("users").document(userId).collection("bookmarks").document(bookId)

        if (isBookmarked) {
            bookmarkRef.delete().addOnSuccessListener {
                isBookmarked = false
                updateBookmarkButtonState()
            }
        } else {
            val bookmarkData = hashMapOf("bookId" to bookId, "timestamp" to FieldValue.serverTimestamp())
            bookmarkRef.set(bookmarkData).addOnSuccessListener {
                isBookmarked = true
                updateBookmarkButtonState()
            }
        }
    }

    private fun updateBookmarkButtonState() {
        val bookmarkedColor = Color.parseColor("#00796B")
        if (isBookmarked) {
            bookmarkButton.text = "Bookmarked"
            bookmarkButton.setIconResource(R.drawable.ic_bookmark_solid)
            bookmarkButton.iconTint = ColorStateList.valueOf(bookmarkedColor)
        } else {
            bookmarkButton.text = "Bookmark"
            bookmarkButton.setIconResource(R.drawable.ic_bookmark_border)
            bookmarkButton.iconTint = ColorStateList.valueOf(bookmarkedColor)
        }
    }

    private fun loadBookDetails(bookId: String, view: View) {
        val bookCoverImageView = view.findViewById<ImageView>(R.id.book_cover_image)
        val bookTitleTextView = view.findViewById<TextView>(R.id.book_title)
        val bookAuthorTextView = view.findViewById<TextView>(R.id.book_author)
        val bookDescriptionTextView = view.findViewById<TextView>(R.id.book_description)

        db.collection("books").document(bookId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val book = document.toObject(Book::class.java)?.copy(id = document.id)
                    if (book != null) {
                        bookTitleTextView.text = book.title
                        bookAuthorTextView.text = book.author
                        bookDescriptionTextView.text = book.description
                        
                        if (book.coverImageUrl != null) {
                            Glide.with(this).load(book.coverImageUrl).into(bookCoverImageView)
                        } else {
                            bookCoverImageView.setImageResource(R.drawable.icon) // Placeholder
                        }
                    }
                }
            }
    }
}
