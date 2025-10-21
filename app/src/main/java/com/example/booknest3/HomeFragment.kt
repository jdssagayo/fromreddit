package com.example.booknest3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // I-lo-load po natin ang inyong design.
        return inflater.inflate(R.layout.home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inayos ko na po ito, nilagyan ko na ng author ang bawat libro.
        val sampleBooks = listOf(
            Book("The Great Gatsby", R.drawable.icon, "F. Scott Fitzgerald"),
            Book("To Kill a Mockingbird", R.drawable.icon, "Harper Lee"),
            Book("1984", R.drawable.icon, "George Orwell"),
            Book("Pride and Prejudice", R.drawable.icon, "Jane Austen"),
            Book("The Catcher in the Rye", R.drawable.icon, "J.D. Salinger"),
            Book("The Hobbit", R.drawable.icon, "J.R.R. Tolkien")
        )

        val onBookClick: (Book) -> Unit = { book ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BookDetailsFragment()) // Bubuksan na po nito ang tamang screen.
                .addToBackStack(null)
                .commit()
        }

        // 2. Hanapin ang RecyclerView para sa "Continue Reading"
        val continueReadingRecycler = view.findViewById<RecyclerView>(R.id.continue_reading_recycler)
        continueReadingRecycler.adapter = BookAdapter(sampleBooks, R.layout.item_book_horizontal, onBookClick)

        // 3. Hanapin ang RecyclerView para sa "Recommend For You"
        val recommendRecycler = view.findViewById<RecyclerView>(R.id.recommend_recycler)
        recommendRecycler.adapter = BookAdapter(sampleBooks, R.layout.item_book_grid, onBookClick)
    }
}
