package com.example.booknest3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class BookmarksFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // I-lo-load po natin dito ang bagong UI para sa bookmarks mamaya.
        return inflater.inflate(R.layout.fragment_bookmarks, container, false)
    }
}
