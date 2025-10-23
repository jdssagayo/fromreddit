package com.example.booknest3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BookmarkAdapter(
    private var bookList: List<Book>,
    private val onItemClick: (Book) -> Unit
) : RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder>() {

    fun updateData(newBookList: List<Book>) {
        this.bookList = newBookList
        notifyDataSetChanged()
    }

    inner class BookmarkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Correct views from your item_bookmark.xml
        private val bookCover: ImageView = itemView.findViewById(R.id.book_cover_image)
        private val bookTitleText: TextView = itemView.findViewById(R.id.book_title_text)
        private val pageSnippetText: TextView = itemView.findViewById(R.id.page_snippet_text)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.read_progress_bar)
        private val progressText: TextView = itemView.findViewById(R.id.progress_percentage_text)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(bookList[position])
                }
            }
        }

        fun bind(book: Book) {
            // Combine chapter and title into the single TextView from your layout
            bookTitleText.text = "${book.chapter ?: "Chapter"} ${book.title}"
            pageSnippetText.text = "Page ${book.page} - ${book.snippet ?: "..."}"
            progressText.text = "${book.progress}%"
            progressBar.progress = book.progress

            if (book.coverImageUrl != null) {
                Glide.with(itemView.context)
                    .load(book.coverImageUrl)
                    .into(bookCover)
            } else {
                bookCover.setImageResource(R.drawable.ic_book_logo) // Use a reliable placeholder
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bookmark, parent, false)
        return BookmarkViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        holder.bind(bookList[position])
    }

    override fun getItemCount() = bookList.size
}
