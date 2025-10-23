package com.example.booknest3

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import java.io.File

class DownloadsFragment : Fragment() {

    private lateinit var downloadsRecyclerView: RecyclerView
    private lateinit var noDownloadsText: TextView
    private lateinit var bookAdapter: BookAdapter
    private val downloadedBooks = mutableListOf<Book>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_downloads, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        downloadsRecyclerView = view.findViewById(R.id.downloads_recycler_view)
        noDownloadsText = view.findViewById(R.id.no_downloads_text)

        setupRecyclerView()
        loadDownloadedBooks()
    }

    private fun setupRecyclerView() {
        // Assuming item_book_download.xml exists for the grid item layout
        bookAdapter = BookAdapter(downloadedBooks, R.layout.item_book_download) { book ->
            val fragment = BookDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString("BOOK_ID", book.id)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        downloadsRecyclerView.adapter = bookAdapter
        downloadsRecyclerView.layoutManager = GridLayoutManager(context, 3)
    }

    private fun loadDownloadedBooks() {
        downloadedBooks.clear()
        val downloadDir = File(requireContext().filesDir, "downloads")

        if (!downloadDir.exists() || downloadDir.listFiles()?.isEmpty() == true) {
            showEmptyState()
            return
        }

        val bookFiles = downloadDir.listFiles { _, name -> name.endsWith(".json") }
        if (bookFiles == null || bookFiles.isEmpty()) {
            showEmptyState()
            return
        }

        for (file in bookFiles) {
            try {
                val jsonString = file.readText()
                val jsonObject = JSONObject(jsonString)
                val book = Book(
                    id = file.nameWithoutExtension,
                    title = jsonObject.optString("title", "No Title"),
                    author = jsonObject.optString("author", "No Author"),
                    description = jsonObject.optString("description", ""),
                    coverImageUrl = jsonObject.optString("coverImageUrl", null)
                )
                downloadedBooks.add(book)
            } catch (e: Exception) {
                Log.e("DownloadsFragment", "Error parsing book file: ${file.name}", e)
            }
        }

        if (downloadedBooks.isEmpty()) {
            showEmptyState()
        } else {
            showDownloadsList()
            bookAdapter.notifyDataSetChanged()
        }
    }

    private fun showEmptyState(message: String = "No downloads yet.") {
        noDownloadsText.text = message
        noDownloadsText.visibility = View.VISIBLE
        downloadsRecyclerView.visibility = View.GONE
    }

    private fun showDownloadsList() {
        noDownloadsText.visibility = View.GONE
        downloadsRecyclerView.visibility = View.VISIBLE
    }
}
