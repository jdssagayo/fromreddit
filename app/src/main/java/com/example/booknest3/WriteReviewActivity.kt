package com.example.booknest3

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WriteReviewActivity : AppCompatActivity() {

    private lateinit var closeButton: ImageView
    private lateinit var reviewInputText: EditText
    private lateinit var charCounter: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var submitButton: TextView // Changed to TextView
    private lateinit var userAvatar: ImageView
    private lateinit var userName: TextView
    private lateinit var toolbarTitle: TextView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var bookId: String? = null
    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_review)

        bookId = intent.getStringExtra("BOOK_ID")
        val existingComment = intent.getStringExtra("EXISTING_COMMENT")
        val existingRating = intent.getFloatExtra("EXISTING_RATING", 0f)
        isEditing = existingComment != null

        closeButton = findViewById(R.id.close_button)
        reviewInputText = findViewById(R.id.review_input_text)
        charCounter = findViewById(R.id.char_counter)
        ratingBar = findViewById(R.id.rating_bar_input) // Corrected ID
        submitButton = findViewById(R.id.post_button) // Corrected ID
        userAvatar = findViewById(R.id.user_avatar)
        userName = findViewById(R.id.user_name)
        toolbarTitle = findViewById(R.id.toolbar_title)

        loadUserData()
        setupUIForEditMode(existingComment, existingRating)

        closeButton.setOnClickListener {
            finish() // Close the activity
        }

        reviewInputText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentLength = s?.length ?: 0
                charCounter.text = "$currentLength/500"
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        submitButton.setOnClickListener {
            submitReview()
        }
    }

    private fun setupUIForEditMode(comment: String?, rating: Float) {
        if (isEditing) {
            toolbarTitle.text = "Edit your review"
            reviewInputText.setText(comment)
            ratingBar.rating = rating
        }
    }

    private fun loadUserData(){
        val user = auth.currentUser
        if (user != null) {
            userName.text = user.displayName
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    val photoUrl = doc.getString("photoUrl")
                    if (!photoUrl.isNullOrEmpty()) {
                        Glide.with(this).load(photoUrl).into(userAvatar)
                    }
                }
        }
    }

    private fun submitReview() {
        val user = auth.currentUser
        if (user == null || bookId == null) {
            Toast.makeText(this, "You must be logged in to leave a review.", Toast.LENGTH_SHORT).show()
            return
        }

        val rating = ratingBar.rating
        val comment = reviewInputText.text.toString().trim()

        if (rating == 0f) {
            Toast.makeText(this, "Please provide a rating.", Toast.LENGTH_SHORT).show()
            return
        }

        submitButton.isEnabled = false // Prevent multiple clicks

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { userDoc ->
                val username = userDoc.getString("username") ?: user.displayName ?: "Anonymous"
                val userProfileUrl = userDoc.getString("photoUrl")

                val review = Review(
                    userId = user.uid,
                    username = username,
                    userProfileUrl = userProfileUrl,
                    rating = rating,
                    comment = comment
                )

                db.collection("books").document(bookId!!)
                    .collection("reviews").document(user.uid)
                    .set(review)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error submitting review: ${e.message}", Toast.LENGTH_SHORT).show()
                        submitButton.isEnabled = true
                    }
            }
            .addOnFailureListener { e ->
                 Toast.makeText(this, "Error fetching user details: ${e.message}", Toast.LENGTH_SHORT).show()
                 submitButton.isEnabled = true
            }
    }
}
