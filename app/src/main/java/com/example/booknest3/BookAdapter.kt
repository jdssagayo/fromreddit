package com.example.booknest3

import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BookAdapter(
    private var bookList: List<Book>,
    private val layoutId: Int,
    private val onItemClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    fun updateBooks(newBooks: List<Book>) {
        bookList = newBooks
        notifyDataSetChanged()
    }

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

        val imageUrl = currentBook.coverImageUrl
        if (!imageUrl.isNullOrEmpty()) {
            try {
                if (imageUrl.startsWith("http")) {
                    // Handle old Firebase Storage URLs
                    Glide.with(holder.itemView.context)
                        .load(imageUrl)
                        .into(holder.bookCover)
                } else {
                    // Handle new Base64 strings
                    val imageBytes = Base64.decode(imageUrl, Base64.DEFAULT)
                    Glide.with(holder.itemView.context)
                        .load(imageBytes)
                        .into(holder.bookCover)
                }
            } catch (e: IllegalArgumentException) {
                // If decoding fails, set a placeholder
                holder.bookCover.setImageResource(R.drawable.icon)
            }
        } else {
            // If no image URL is provided, set a placeholder
            holder.bookCover.setImageResource(R.drawable.icon)
        }

        holder.bookTitle?.text = currentBook.title
        holder.bookAuthor?.text = currentBook.author
    }

    override fun getItemCount() = bookList.size
}
