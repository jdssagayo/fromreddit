package com.example.booknest3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class HomeFragment : Fragment() {

    private lateinit var recommendRecycler: RecyclerView
    private lateinit var continueReadingRecycler: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recommendedBooksAdapter: BookAdapter
    private lateinit var continueReadingAdapter: BookAdapter

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recommendRecycler = view.findViewById(R.id.recommend_recycler)
        continueReadingRecycler = view.findViewById(R.id.continue_reading_recycler)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)

        setupRecyclerViews()
        loadAllBooks()

        swipeRefreshLayout.setOnRefreshListener {
            loadAllBooks()
        }
    }

    private fun setupRecyclerViews() {
        val onBookClick: (Book) -> Unit = { book ->
            val bundle = Bundle()
            bundle.putString("BOOK_ID", book.id)
            val detailsFragment = BookDetailsFragment()
            detailsFragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, detailsFragment)
                .addToBackStack(null)
                .commit()
        }

        recommendedBooksAdapter = BookAdapter(emptyList(), R.layout.item_book_grid, onBookClick)
        recommendRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
        recommendRecycler.adapter = recommendedBooksAdapter

        continueReadingAdapter = BookAdapter(emptyList(), R.layout.item_book_horizontal, onBookClick)
        continueReadingRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        continueReadingRecycler.adapter = continueReadingAdapter
    }

    private fun loadAllBooks() {
        swipeRefreshLayout.isRefreshing = true
        loadRecommendedBooks()
        loadContinueReadingBooks()
    }

    private fun loadRecommendedBooks() {
        db.collection("books").orderBy("publishedAt", Query.Direction.DESCENDING).limit(10).get()
            .addOnSuccessListener { documents ->
                val books = documents.map { doc ->
                    doc.toObject(Book::class.java).copy(id = doc.id)
                }
                recommendedBooksAdapter.updateBooks(books)
                swipeRefreshLayout.isRefreshing = false // Stop refreshing indicator
            }
            .addOnFailureListener { 
                swipeRefreshLayout.isRefreshing = false
            }
    }

    private fun loadContinueReadingBooks() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("reading_progress")
            .orderBy("lastAccessed", Query.Direction.DESCENDING).limit(5).get()
            .addOnSuccessListener { documents ->
                val bookIds = documents.map { it.id }
                if (bookIds.isNotEmpty()) {
                    db.collection("books").whereIn("__name__", bookIds).get()
                        .addOnSuccessListener { bookDocs ->
                            val books = bookDocs.map { doc -> doc.toObject(Book::class.java).copy(id = doc.id) }
                            val sortedBooks = bookIds.mapNotNull { id -> books.find { it.id == id } }
                            continueReadingAdapter.updateBooks(sortedBooks)
                        }
                }
            }
    }
}
