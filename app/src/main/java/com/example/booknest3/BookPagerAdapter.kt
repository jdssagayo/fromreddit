package com.example.booknest3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class BookPagerAdapter(
    private val bookList: List<Book>,
    private val onItemClick: (Book) -> Unit // Dinagdag ko na po ito para umayon sa bagong BookAdapter
) : RecyclerView.Adapter<BookPagerAdapter.BookPagerViewHolder>() {

    inner class BookPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookAdapter = BookAdapter(bookList, R.layout.item_book_grid, onItemClick) // Inayos ko na po ito
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookPagerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book_pager, parent, false)
        return BookPagerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookPagerViewHolder, position: Int) {
        val recyclerView = holder.itemView.findViewById<RecyclerView>(R.id.pager_recycler_view)
        recyclerView.adapter = holder.bookAdapter
    }

    override fun getItemCount() = 1
}
