package com.example.booknest3

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class TextPageAdapter(activity: FragmentActivity, private val pages: List<String>) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = pages.size

    override fun createFragment(position: Int): Fragment {
        return BookPageFragment.newInstance(pages[position])
    }
}
