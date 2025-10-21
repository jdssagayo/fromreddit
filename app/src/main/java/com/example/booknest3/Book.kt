package com.example.booknest3

// This is the blueprint for a single book.
// Inayos ko na po para may kasama nang author.
data class Book(
    val title: String,
    val coverImage: Int,
    val author: String
)
