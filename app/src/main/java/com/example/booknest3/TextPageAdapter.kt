package com.example.booknest3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TextPageAdapter(private val pages: List<String>) :
    RecyclerView.Adapter<TextPageAdapter.PageViewHolder>() {

    class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pageTextView: TextView = itemView.findViewById(R.id.page_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_text_page, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.pageTextView.text = pages[position]
    }

    override fun getItemCount(): Int {
        return pages.size
    }
}
