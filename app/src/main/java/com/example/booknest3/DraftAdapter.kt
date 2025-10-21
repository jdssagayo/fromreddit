package com.example.booknest3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class DraftAdapter(
    private val draftList: List<Draft>,
    private val onItemClick: (Draft) -> Unit
) : RecyclerView.Adapter<DraftAdapter.DraftViewHolder>() {

    inner class DraftViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Inayos ko na po ang ID para tumugma sa layout.
        private val titleTextView: TextView = itemView.findViewById(R.id.draft_book_title)
        private val lastModifiedTextView: TextView = itemView.findViewById(R.id.draft_last_modified)

        fun bind(draft: Draft) {
            titleTextView.text = draft.title.ifEmpty { "(No Title)" } 
            lastModifiedTextView.text = draft.lastModified?.let {
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)
            } ?: "Not available"

            itemView.setOnClickListener { onItemClick(draft) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DraftViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_draft, parent, false)
        return DraftViewHolder(view)
    }

    override fun onBindViewHolder(holder: DraftViewHolder, position: Int) {
        holder.bind(draftList[position])
    }

    override fun getItemCount() = draftList.size
}
