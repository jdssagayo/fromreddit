package com.example.booknest3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot

class PageAdapter(
    private var pages: List<DocumentSnapshot>,
    private val onPageClick: (position: Int) -> Unit,
    private val onDeleteListener: (pageId: String) -> Unit
) : RecyclerView.Adapter<PageAdapter.PageViewHolder>() {

    class PageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pageTitle: TextView = view.findViewById(R.id.page_title_text)
        val wordCount: TextView = view.findViewById(R.id.page_word_count_text)
        val deleteButton: ImageButton = view.findViewById(R.id.delete_page_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_page, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val pageSnapshot = pages[position]
        val pageData = pageSnapshot.data

        val pageNumber = pageData?.get("pageNumber") as? Long ?: (position + 1)
        val content = pageData?.get("content") as? String ?: ""
        val wordCount = content.split(Regex("\\s+")).filter { it.isNotEmpty() }.size

        holder.pageTitle.text = "Page $pageNumber"
        holder.wordCount.text = "$wordCount words"

        holder.itemView.setOnClickListener { 
            onPageClick(position)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteListener(pageSnapshot.id)
        }
    }

    override fun getItemCount() = pages.size

    fun updatePages(newPages: List<DocumentSnapshot>) {
        pages = newPages
        notifyDataSetChanged()
    }
}
