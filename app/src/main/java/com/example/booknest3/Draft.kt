package com.example.booknest3

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Draft(
    @DocumentId
    val id: String = "",

    val title: String = "",
    val description: String = "",
    val content: String = "",

    // New fields to link pages into a book
    val bookId: String = "", // ID to group pages of the same book
    val pageNumber: Int = 1,     // The page number within the book

    @ServerTimestamp
    val lastModified: Date? = null
)
