package com.example.booknest3

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.lang.IllegalArgumentException

class ProfileFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var bookAdapter: BookAdapter
    private lateinit var tabLayout: TabLayout

    // Stat TextViews
    private lateinit var statBooksReadText: TextView
    private lateinit var statPublishedText: TextView
    private lateinit var statStreakText: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backArrow = view.findViewById<ImageView>(R.id.back_arrow)
        val editProfileButton = view.findViewById<Button>(R.id.edit_profile_button)
        val booksRecyclerView = view.findViewById<RecyclerView>(R.id.books_recycler_view)
        val profileNameTextView = view.findViewById<TextView>(R.id.profile_name)
        val profileHandleTextView = view.findViewById<TextView>(R.id.profile_handle)
        val profileBioTextView = view.findViewById<TextView>(R.id.profile_bio)
        val profileImageView = view.findViewById<ImageView>(R.id.profile_image)
        tabLayout = view.findViewById(R.id.tab_layout)

        // Init Stat TextViews
        statBooksReadText = view.findViewById(R.id.stat_books_read_text)
        statPublishedText = view.findViewById(R.id.stat_published_text)
        statStreakText = view.findViewById(R.id.stat_streak_text)

        // Back Arrow Logic
        backArrow.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Edit Profile Button Logic
        editProfileButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EditProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        // Setup RecyclerView
        booksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        bookAdapter = BookAdapter(emptyList(), R.layout.item_book_profile) { book ->
            val bundle = Bundle()
            bundle.putString("BOOK_ID", book.id)
            val detailsFragment = BookDetailsFragment()
            detailsFragment.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, detailsFragment)
                .addToBackStack(null)
                .commit()
        }
        booksRecyclerView.adapter = bookAdapter

        loadUserProfile(profileNameTextView, profileHandleTextView, profileBioTextView, profileImageView)
        loadUserStats()
        setupTabs()
    }

    private fun setupTabs() {
        // Load the default tab's content
        loadReadingBooks()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadReadingBooks()
                    1 -> loadDoneBooks()
                    2 -> loadPublishedBooks()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

     private fun loadUserStats() {
        val userId = auth.currentUser?.uid ?: return

        // Get "Books Read" count
        db.collection("users").document(userId).collection("reading_progress")
            .whereEqualTo("progress", 100)
            .get()
            .addOnSuccessListener { documents ->
                statBooksReadText.text = "Books read\n${documents.size()}"
            }
            .addOnFailureListener { e ->
                Log.e("ProfileFragment", "Error loading books read count", e)
                Toast.makeText(context, "Couldn't load 'Books Read' stat: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        // Get "Published" count
        db.collection("books")
            .whereEqualTo("authorId", userId)
            .get()
            .addOnSuccessListener { documents ->
                statPublishedText.text = "Published\n${documents.size()}"
            }
            .addOnFailureListener { e ->
                Log.e("ProfileFragment", "Error loading published count", e)
                Toast.makeText(context, "Couldn't load 'Published' stat: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        // Streak is static for now
        statStreakText.text = "Streak\n7 days"
    }

    private fun loadReadingBooks() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("reading_progress")
            .whereLessThan("progress", 100) // Assumes a 'progress' field
            .get()
            .addOnSuccessListener { documents ->
                fetchBooksFromIds(documents.map { it.id })
            }
            .addOnFailureListener { e ->
                 Log.e("ProfileFragment", "Error loading reading books", e)
            }
    }

    private fun loadDoneBooks() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("reading_progress")
            .whereEqualTo("progress", 100) // Assumes a 'progress' field
            .get()
            .addOnSuccessListener { documents ->
                fetchBooksFromIds(documents.map { it.id })
            }
             .addOnFailureListener { e ->
                 Log.e("ProfileFragment", "Error loading done books", e)
            }
    }

    private fun loadPublishedBooks() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("books")
            .whereEqualTo("authorId", userId)
            .orderBy("publishedAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val userBooks = documents.map { doc ->
                    doc.toObject(Book::class.java).copy(id = doc.id)
                }
                bookAdapter.updateBooks(userBooks)
            }
            .addOnFailureListener { e ->
                 Log.e("ProfileFragment", "Error loading published books", e)
            }
    }

    private fun fetchBooksFromIds(bookIds: List<String>) {
        if (bookIds.isEmpty()) {
            bookAdapter.updateBooks(emptyList()) // Clear the list if no IDs
            return
        }
        db.collection("books").whereIn("__name__", bookIds).get()
            .addOnSuccessListener { bookDocs ->
                val books = bookDocs.map { doc -> doc.toObject(Book::class.java).copy(id = doc.id) }
                val sortedBooks = bookIds.mapNotNull { id -> books.find { it.id == id } } // Preserve order
                bookAdapter.updateBooks(sortedBooks)
            }
            .addOnFailureListener { e ->
                 Log.e("ProfileFragment", "Error fetching books by ID", e)
            }
    }

    private fun loadUserProfile(profileNameTextView: TextView, profileHandleTextView: TextView, profileBioTextView: TextView, profileImageView: ImageView) {
        val userId = auth.currentUser?.uid ?: return

        auth.currentUser?.displayName?.let {
            profileNameTextView.text = it
        }

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    profileHandleTextView.text = "@" + document.getString("username")
                    profileBioTextView.text = document.getString("bio")
                    val photoUrl = document.getString("photoUrl")
                    if (!photoUrl.isNullOrEmpty()) {
                        try {
                            if (photoUrl.startsWith("http")) {
                                Glide.with(this@ProfileFragment).load(photoUrl).into(profileImageView)
                            } else {
                                val imageBytes = Base64.decode(photoUrl, Base64.DEFAULT)
                                Glide.with(this@ProfileFragment).load(imageBytes).into(profileImageView)
                            }
                        } catch (e: IllegalArgumentException) {
                            Log.e("ProfileFragment", "Error decoding Base64 image", e)
                        }
                    }
                } else {
                    Log.d("ProfileFragment", "No such document")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileFragment", "Error loading profile", e)
                Toast.makeText(context, "Failed to load profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
