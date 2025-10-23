package com.example.booknest3

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WriteReviewActivity : AppCompatActivity() {

    private lateinit var closeButton: ImageView
    private lateinit var reviewInputText: EditText
    private lateinit var charCounter: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_review)

        closeButton = findViewById(R.id.close_button)
        reviewInputText = findViewById(R.id.review_input_text)
        charCounter = findViewById(R.id.char_counter)

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
    }
}
