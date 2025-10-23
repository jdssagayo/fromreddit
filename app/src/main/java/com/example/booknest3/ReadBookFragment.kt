package com.example.booknest3

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ReadBookFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var arrowLeft: ImageView
    private lateinit var arrowRight: ImageView
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var bookId: String? = null
    private var pages: List<String> = emptyList()
    private var isContentLoaded = false // Flag to prevent saving before content is loaded

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bookId = arguments?.getString("BOOK_ID")
        return inflater.inflate(R.layout.read_book, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_read)
        viewPager = view.findViewById(R.id.book_content_pager)
        arrowLeft = view.findViewById(R.id.arrow_left)
        arrowRight = view.findViewById(R.id.arrow_right)

        toolbar.navigationIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        bookId?.let { loadBookContent(it) }

        setupPageTurnListeners()
    }

    override fun onStop() {
        super.onStop()
        // Only save progress if the content has actually been loaded.
        if (isContentLoaded) {
            saveReadingProgress()
        }
    }

    private fun saveReadingProgress() {
        val userId = auth.currentUser?.uid
        val currentBookId = bookId
        // The isContentLoaded flag ensures pages is not empty.
        if (userId == null || currentBookId == null) return

        val currentPageIndex = viewPager.currentItem
        // Use floating point division for accuracy, then convert to Int.
        val progress = (((currentPageIndex + 1).toFloat() / pages.size) * 100).toInt()
        val snippet = pages.getOrNull(currentPageIndex)?.take(50) ?: ""

        val bookmarkData = hashMapOf<String, Any>(
            "page" to currentPageIndex.toLong(),
            "progress" to progress.toLong(),
            "snippet" to snippet,
            "lastAccessed" to FieldValue.serverTimestamp()
        )

        db.collection("users").document(userId)
            .collection("bookmarks").document(currentBookId)
            .set(bookmarkData, SetOptions.merge())
    }

    private fun loadBookContent(bookId: String) {
        db.collection("books").document(bookId).get()
            .addOnSuccessListener { document ->
                if (document != null && context != null) { // Check for context != null as well
                    val content = document.getString("content") ?: ""
                    this.pages = splitTextIntoPages(content, 300)
                    val adapter = BookPagerAdapter(this, this.pages)
                    viewPager.adapter = adapter
                    updateArrowVisibility()
                    isContentLoaded = true // Set flag here, after everything is ready.
                }
            }
    }

    private fun setupPageTurnListeners() {
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateArrowVisibility()
            }
        })

        arrowLeft.setOnClickListener {
            viewPager.currentItem = viewPager.currentItem - 1
        }

        arrowRight.setOnClickListener {
            viewPager.currentItem = viewPager.currentItem + 1
        }
    }

    private fun splitTextIntoPages(text: String, wordsPerPage: Int): List<String> {
        val words = text.split(Regex("\\s+"))
        val pages = mutableListOf<String>()
        var currentPage = StringBuilder()
        var wordCount = 0

        for (word in words) {
            currentPage.append(word).append(" ")
            wordCount++
            if (wordCount >= wordsPerPage) {
                pages.add(currentPage.toString().trim())
                currentPage = StringBuilder()
                wordCount = 0
            }
        }

        if (currentPage.isNotEmpty()) {
            pages.add(currentPage.toString().trim())
        }
        return pages
    }

    private fun updateArrowVisibility() {
        val currentPosition = viewPager.currentItem
        val totalPages = viewPager.adapter?.itemCount ?: 0

        arrowLeft.visibility = if (currentPosition > 0) View.VISIBLE else View.INVISIBLE
        arrowRight.visibility = if (currentPosition < totalPages - 1) View.VISIBLE else View.INVISIBLE
    }
}
