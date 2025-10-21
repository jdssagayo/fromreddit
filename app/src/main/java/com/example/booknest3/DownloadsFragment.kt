package com.example.booknest3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView

class DownloadsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Ilo-load na po natin ang tamang layout.
        return inflater.inflate(R.layout.fragment_downloads, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val downloadsRecyclerView = view.findViewById<RecyclerView>(R.id.downloads_recycler_view)

        // Sample data - palitan niyo na lang po ito ng actual downloaded books ninyo.
        val downloadedBooks = listOf(
            Book("AfterLife", R.drawable.ic_launcher_background, "Lavel Shake"),
            Book("Good Company", R.drawable.ic_launcher_background, "Cynthia D'aprix"),
            Book("Leaving Time", R.drawable.ic_launcher_background, "Jodi Picoult"),
            Book("Ghost Boys", R.drawable.ic_launcher_background, "Jewel Parker Rhodes"),
            Book("The Vanishing Half", R.drawable.ic_launcher_background, "Brit Bennett"),
            Book("The Midnight Library", R.drawable.ic_launcher_background, "Matt Haig")
        )

        // Gagamitin po natin ang BookAdapter na may bagong item layout.
        downloadsRecyclerView.adapter = BookAdapter(downloadedBooks, R.layout.item_book_download) { book ->
            // Dito po ilalagay ang code kapag may pinindot na libro.
        }
    }
}
