package com.example.booknest3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment

class BookDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.book_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // CHECKPOINT: Magpapakita po ito ng message para malaman natin kung nakarating tayo sa screen na ito.
        Toast.makeText(requireContext(), "Book Details Screen Loaded!", Toast.LENGTH_SHORT).show()

        val moreOptionsButton = view.findViewById<ImageView>(R.id.more_options)

        moreOptionsButton.setOnClickListener { anchorView ->
            val popup = PopupMenu(requireContext(), anchorView)
            popup.menuInflater.inflate(R.menu.book_details_menu, popup.menu)

            // Heto po ang code para pilitin na ipakita ang mga icons.
            try {
                val fieldMPopup = popup.javaClass.getDeclaredField("mPopup")
                fieldMPopup.isAccessible = true
                val mPopup = fieldMPopup.get(popup)
                mPopup.javaClass
                    .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                    .invoke(mPopup, true)
            } catch (e: Exception) {
                // Baka hindi gumana sa ibang version, pero at least sinubukan po natin.
            }

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_save_draft -> {
                        Toast.makeText(requireContext(), "Save Draft Clicked!", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.action_export_book -> {
                        Toast.makeText(requireContext(), "Export Book Clicked!", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }

            popup.show()
        }
    }
}
