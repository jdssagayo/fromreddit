package com.example.booknest3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class ReadBookActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.read_book)

        val viewPager: ViewPager2 = findViewById(R.id.book_view_pager)

        // This is where you would get your book's content from your data source.
        // For now, we'll use a long placeholder string and split it.
        val longText = """Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Aliquam nibh. Mauris ac mauris sed pede pellentesque fermentum. Maecenas adipiscing ante non diam sodales hendrerit. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\n\nUt enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.\n\nCurabitur pretium tincidunt lacus. Nulla gravida orci a odio. Nullam varius, turpis et commodo pharetra, est eros bibendum elit, nec luctus magna felis sollicitudin mauris. Integer in mauris eu nibh euismod gravida. Duis ac tellus et risus vulputate vehicula. Donec lobortis, risus a elit. Etiam tempor. Ut ullamcorper, ligula eu tempor congue, eros est euismod turpis, id tincidunt sapien risus a quam. Nunc turpis ullamcorper nibh, in tempus sapien eros vitae ligula. Pellentesque rhoncus nunc et augue.""".repeat(5) // Repeat to create more pages

        val pages = splitTextIntoPages(longText, 1000) // Approx 1000 chars per page

        val pagerAdapter = BookPagerAdapter(this, pages)
        viewPager.adapter = pagerAdapter
    }

    // This is a simple text splitting function. You may need a more sophisticated one.
    private fun splitTextIntoPages(text: String, charsPerPage: Int): List<String> {
        val pages = mutableListOf<String>()
        var i = 0
        while (i < text.length) {
            // Find the last space before the character limit to avoid splitting words
            var end = (i + charsPerPage).coerceAtMost(text.length)
            if (end < text.length) {
                end = text.lastIndexOf(' ', end).coerceAtLeast(i + 1)
            }

            pages.add(text.substring(i, end).trim())
            i = end
        }
        return pages
    }
}
