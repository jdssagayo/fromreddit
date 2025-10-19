import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.compose.ui.layout.layout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        // Find your views from the XML layout
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val fabDraft = findViewById<FloatingActionButton>(R.id.fab_draft)

        // --- THIS IS THE REUSABLE NAVIGATION LOGIC ---
        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Navigate to Home or refresh the current screen
                    Toast.makeText(this, "Home Clicked", Toast.LENGTH_SHORT).show()
                    true // Return true to show the item as selected
                }
                R.id.navigation_save -> {
                    // Navigate to Saved screen/fragment
                    Toast.makeText(this, "Save Clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.navigation_drafts -> {
                    // Navigate to Drafts screen/fragment
                    Toast.makeText(this, "Drafts Clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.navigation_downloads -> {
                    // Navigate to Downloads screen/fragment
                    Toast.makeText(this, "Downloads Clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.navigation_profile -> {
                    // Navigate to Profile screen/fragment
                    Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false // Return false for items you don't handle
            }
        }

        // --- Optional: Handle the Floating Action Button click ---
        fabDraft.setOnClickListener {
            Toast.makeText(this, "Create Draft Clicked", Toast.LENGTH_SHORT).show()
            // Start a new activity to create a draft, for example:
            // val intent = Intent(this, CreateDraftActivity::class.java)
            // startActivity(intent)
        }
    }
}
