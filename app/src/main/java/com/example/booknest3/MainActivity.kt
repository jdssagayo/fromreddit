package com.example.booknest3

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        bottomNav = findViewById(R.id.bottom_nav)

        if (savedInstanceState == null) {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                // No user is signed in, show login screen
                replaceFragment(LoginFragment(), false)
            } else {
                // User is signed in, go to home screen
                replaceFragment(HomeFragment(), false)
                bottomNav.selectedItemId = R.id.nav_home
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_bookmarks -> BookmarkFragment()
                R.id.nav_edit -> DraftListFragment()
                R.id.nav_downloads -> DownloadsFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> HomeFragment()
            }
            replaceFragment(selectedFragment)
            true
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser == null) {
            replaceFragment(LoginFragment(), false)
        }
    }

    fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()

        if (fragment is LoginFragment || fragment is SignupFragment) {
            bottomNav.visibility = View.GONE
        } else {
            bottomNav.visibility = View.VISIBLE
        }
    }

    fun navigateToHome() {
        replaceFragment(HomeFragment(), false)
        bottomNav.selectedItemId = R.id.nav_home
    }
}
