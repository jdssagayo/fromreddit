package com.example.booknest3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment

class PageFragment : Fragment() {

    private lateinit var contentEditText: EditText
    private lateinit var contentReadText: TextView
    private var pageContent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageContent = arguments?.getString(ARG_CONTENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentEditText = view.findViewById(R.id.content_edit_text_page)
        contentReadText = view.findViewById(R.id.content_read_text_page)

        contentEditText.setText(pageContent)
        contentReadText.text = pageContent
    }

    fun toggleEditMode(isEditing: Boolean) {
        if (isEditing) {
            contentReadText.visibility = View.GONE
            contentEditText.visibility = View.VISIBLE
        } else {
            contentReadText.visibility = View.VISIBLE
            contentEditText.visibility = View.GONE
        }
    }

    fun getContent(): String {
        return contentEditText.text.toString()
    }

    companion object {
        private const val ARG_CONTENT = "content"

        fun newInstance(content: String): PageFragment {
            val fragment = PageFragment()
            val args = Bundle()
            args.putString(ARG_CONTENT, content)
            fragment.arguments = args
            return fragment
        }
    }
}
