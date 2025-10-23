package com.example.booknest3

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Review(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatarUrl: String? = null,
    val rating: Float = 0f,
    val reviewText: String = "",
    @ServerTimestamp val timestamp: Date? = null
)
