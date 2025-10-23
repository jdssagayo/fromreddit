package com.example.booknest3

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ReadBookActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BOOK_ID = "extra_book_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.read_book)

        val viewPager: ViewPager2 = findViewById(R.id.book_content_pager)
        val firestore = FirebaseFirestore.getInstance()

        // Get the Book ID passed from the previous screen
        val bookId = intent.getStringExtra(EXTRA_BOOK_ID)

        if (bookId == null) {
            Toast.makeText(this, "Error: Book ID not provided.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Fetch the pages for the given book from Firestore
        firestore.collection("books").document(bookId).collection("pages")
            .orderBy("pageNumber", Query.Direction.ASCENDING) // Assumes you have a 'pageNumber' field for ordering
            .get()
            .addOnSuccessListener { pageDocuments ->
                if (pageDocuments.isEmpty) {
                    Toast.makeText(this, "This book has no pages yet.", Toast.LENGTH_SHORT).show()
                    val pagerAdapter = TextPageAdapter(this, listOf("The content for this book is not available yet."))
                    viewPager.adapter = pagerAdapter
                } else {
                    // Assumes each page document has a "content" field
                    val pages = pageDocuments.map { it.getString("content") ?: "" }
                    val pagerAdapter = TextPageAdapter(this, pages)
                    viewPager.adapter = pagerAdapter
                }
            }
            .addOnFailureListener { e ->
                Log.e("ReadBookActivity", "Error fetching pages", e)
                Toast.makeText(this, "Failed to load book.", Toast.LENGTH_SHORT).show()
                val pagerAdapter = TextPageAdapter(this, listOf("Failed to load content. Please check your connection and try again."))
                viewPager.adapter = pagerAdapter
            }
    }
}
