package com.example.booknest3

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Review(
    val userId: String = "",
    val username: String = "",
    val userProfileUrl: String? = null,
    val rating: Float = 0f,
    val comment: String = "",
    @ServerTimestamp val timestamp: Date? = null
)
