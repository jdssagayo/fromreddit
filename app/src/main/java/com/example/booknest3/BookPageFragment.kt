package com.example.booknest3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class BookPageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_book_page, container, false)
        val pageContent = view.findViewById<TextView>(R.id.page_content_text)
        pageContent.text = arguments?.getString(ARG_PAGE_CONTENT)
        return view
    }

    companion object {
        private const val ARG_PAGE_CONTENT = "page_content"

        fun newInstance(pageContent: String): BookPageFragment {
            val fragment = BookPageFragment()
            val args = Bundle()
            args.putString(ARG_PAGE_CONTENT, pageContent)
            fragment.arguments = args
            return fragment
        }
    }
}
