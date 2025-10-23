package com.example.booknest3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ProfileFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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
        val bookAdapter = BookAdapter(emptyList(), R.layout.item_book_profile) { book ->
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

        loadUserProfile(profileNameTextView, profileHandleTextView, profileBioTextView, bookAdapter)
    }

    private fun loadUserProfile(profileNameTextView: TextView, profileHandleTextView: TextView, profileBioTextView: TextView, bookAdapter: BookAdapter) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            // Not signed in, handle appropriately
            return
        }

        // Load user's name
        auth.currentUser?.displayName?.let {
            profileNameTextView.text = it
        }

        // Load user data from Firestore
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    profileHandleTextView.text = "@" + document.getString("username")
                    profileBioTextView.text = document.getString("bio")
                }
            }

        // Load user's published books
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
            .addOnFailureListener {
                // Handle failure to load books
            }
    }
}
