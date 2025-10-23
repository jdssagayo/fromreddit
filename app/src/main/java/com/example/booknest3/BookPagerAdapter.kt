package com.example.booknest3

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class BookPagerAdapter(fragment: Fragment, private val pages: List<String>) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = pages.size

    override fun createFragment(position: Int): Fragment {
        return BookPageFragment.newInstance(pages[position])
    }
}
