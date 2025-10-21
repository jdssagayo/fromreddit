package com.example.booknest3

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        if (savedInstanceState == null) {
            // Ibinalik ko na po ang LoginFragment bilang unang screen para sa Firebase Auth.
            replaceFragment(LoginFragment(), false)
        }

        bottomNav.setOnItemSelectedListener { item ->
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_bookmarks -> BookmarksFragment()
                R.id.nav_edit -> DraftListFragment()
                R.id.nav_downloads -> DownloadsFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> HomeFragment()
            }
            replaceFragment(selectedFragment)
            true
        }
    }

    // Inalis ko na po ang "private" para magamit ng ibang Fragments.
    fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()

        // I-handle ang visibility ng bottom nav.
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        if (fragment is LoginFragment || fragment is SignupFragment) {
            bottomNav.visibility = View.GONE
        } else {
            bottomNav.visibility = View.VISIBLE
        }
    }
}
