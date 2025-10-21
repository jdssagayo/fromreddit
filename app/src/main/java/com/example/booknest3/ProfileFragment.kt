package com.example.booknest3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backArrow = view.findViewById<ImageView>(R.id.back_arrow)
        val signOutButton = view.findViewById<Button>(R.id.sign_out_button)
        val booksRecyclerView = view.findViewById<RecyclerView>(R.id.books_recycler_view)

        // Back Arrow Logic
        backArrow.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Sign Out Button Logic
        signOutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            (activity as? MainActivity)?.replaceFragment(LoginFragment(), false)
        }

        // Setup RecyclerView
        booksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val sampleBooks = listOf(
            Book("Beyond the Ocean Door", R.drawable.icon, "by Amisha Sathi"),
            Book("Really good actually", R.drawable.icon, "by Monica Heisey")
        )
        booksRecyclerView.adapter = BookAdapter(sampleBooks, R.layout.item_book_profile) { /* Click listener */ }
    }
}
