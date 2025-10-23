package com.example.booknest3

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView

class EditorPageAdapter(
    private val pages: MutableList<Page>,
    private val onTextChange: () -> Unit
) : RecyclerView.Adapter<EditorPageAdapter.PageViewHolder>() {

    inner class PageViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.item_editor_page, parent, false)) {

        val pageEditText: EditText = itemView.findViewById(R.id.pageEditText)
        private var textWatcher: TextWatcher? = null

        fun bind(position: Int) {
            textWatcher?.let { pageEditText.removeTextChangedListener(it) }
            pageEditText.setText(pages[position].content)

            val newWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(editable: Editable?) {
                    val currentPosition = adapterPosition
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        pages[currentPosition].content = editable.toString()
                        onTextChange()
                    }
                }
            }
            pageEditText.addTextChangedListener(newWatcher)
            textWatcher = newWatcher
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PageViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = pages.size
}
