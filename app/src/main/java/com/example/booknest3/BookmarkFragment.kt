package com.example.booknest3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class BookmarkFragment : Fragment() {

    private lateinit var bookmarksRecyclerView: RecyclerView
    private lateinit var noBookmarksText: TextView
    private lateinit var bookmarkAdapter: BookmarkAdapter
    private val bookmarkedBooks = mutableListOf<Book>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var bookmarksListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bookmark, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bookmarksRecyclerView = view.findViewById(R.id.bookmarks_recycler_view)
        noBookmarksText = view.findViewById(R.id.no_bookmarks_text)

        setupRecyclerView()
        loadBookmarkedBooks()
    }

    private fun setupRecyclerView() {
        bookmarkAdapter = BookmarkAdapter(bookmarkedBooks) { book ->
            val fragment = BookDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString("BOOK_ID", book.id)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        bookmarksRecyclerView.adapter = bookmarkAdapter
        bookmarksRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun loadBookmarkedBooks() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            showEmptyState()
            return
        }

        val bookmarksRef = db.collection("users").document(userId).collection("bookmarks")
        
        bookmarksListener = bookmarksRef.addSnapshotListener { bookmarkSnapshots, error ->
            if (error != null) {
                showEmptyState("Failed to load bookmarks.")
                return@addSnapshotListener
            }

            if (bookmarkSnapshots == null || bookmarkSnapshots.isEmpty) {
                showEmptyState()
                return@addSnapshotListener
            }

            // Map to hold bookmark details with bookId as key
            val bookmarkDetailsMap = bookmarkSnapshots.documents.associate { doc ->
                doc.id to doc.data
            }
            
            val bookIds = bookmarkSnapshots.documents.map { it.id }

            if (bookIds.isEmpty()) {
                showEmptyState()
                return@addSnapshotListener
            }

            db.collection("books").whereIn(com.google.firebase.firestore.FieldPath.documentId(), bookIds)
                .get()
                .addOnSuccessListener { bookDocuments ->
                    val newBookList = mutableListOf<Book>()
                    for (doc in bookDocuments) {
                        val book = doc.toObject(Book::class.java).copy(id = doc.id)
                        val details = bookmarkDetailsMap[doc.id]
                        
                        // Populate book object with bookmark details
                        book.chapter = details?.get("chapter") as? String
                        book.snippet = details?.get("snippet") as? String
                        (details?.get("page") as? Long)?.let { book.page = it.toInt() }
                        (details?.get("progress") as? Long)?.let { book.progress = it.toInt() }

                        newBookList.add(book)
                    }

                    bookmarkedBooks.clear()
                    bookmarkedBooks.addAll(newBookList)
                    bookmarkAdapter.updateData(newBookList)

                    if (bookmarkedBooks.isEmpty()) {
                        showEmptyState()
                    } else {
                        showBookmarksList()
                    }
                }
                .addOnFailureListener {
                    showEmptyState("Failed to load book details.")
                }
        }
    }

    private fun showEmptyState(message: String = "No bookmarks yet.") {
        bookmarkedBooks.clear()
        bookmarkAdapter.updateData(emptyList())
        noBookmarksText.text = message
        noBookmarksText.visibility = View.VISIBLE
        bookmarksRecyclerView.visibility = View.GONE
    }

    private fun showBookmarksList() {
        noBookmarksText.visibility = View.GONE
        bookmarksRecyclerView.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bookmarksListener?.remove() // Clean up the listener
    }
}
