package com.example.booknest3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BookAdapter(
    private val bookList: List<Book>,
    private val layoutId: Int,
    private val onItemClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Inayos ko na po ang mga IDs para tumugma sa item_book_download.xml
        val bookCover: ImageView = itemView.findViewById(R.id.book_cover_image)
        val bookTitle: TextView? = itemView.findViewById(R.id.book_title)
        val bookAuthor: TextView? = itemView.findViewById(R.id.book_author)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(bookList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val currentBook = bookList[position]
        holder.bookCover.setImageResource(currentBook.coverImage)
        holder.bookTitle?.text = currentBook.title
        // Idinagdag ko na po ang pag-set ng author.
        holder.bookAuthor?.text = currentBook.author
    }

    override fun getItemCount() = bookList.size
}
