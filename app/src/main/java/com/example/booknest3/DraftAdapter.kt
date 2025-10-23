package com.example.booknest3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DraftAdapter(
    private var drafts: List<Draft>,
    private val onDraftClickListener: (Draft) -> Unit,
    private val onDeleteClickListener: (Draft) -> Unit
) : RecyclerView.Adapter<DraftAdapter.DraftViewHolder>() {

    class DraftViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.draft_book_title)
        val description: TextView = view.findViewById(R.id.draft_description_text)
        val editButton: ImageView = view.findViewById(R.id.edit_icon)
        val deleteButton: ImageView = view.findViewById(R.id.delete_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DraftViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_draft, parent, false)
        return DraftViewHolder(view)
    }

    override fun onBindViewHolder(holder: DraftViewHolder, position: Int) {
        val draft = drafts[position]
        holder.title.text = draft.title?.ifEmpty { "New Draft" }
        holder.description.text = draft.description

        // Click listener for the whole item (navigate to edit)
        holder.itemView.setOnClickListener {
            onDraftClickListener(draft)
        }

        // Also allow clicking the edit icon
        holder.editButton.setOnClickListener {
            onDraftClickListener(draft)
        }

        // Click listener for the delete button
        holder.deleteButton.setOnClickListener {
            onDeleteClickListener(draft)
        }
    }

    override fun getItemCount() = drafts.size

    fun updateDrafts(newDrafts: List<Draft>) {
        drafts = newDrafts
        notifyDataSetChanged()
    }
}
