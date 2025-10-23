package com.example.booknest3

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Book(
    @DocumentId val id: String = "",
    val title: String = "",
    val author: String = "",
    val authorId: String = "",
    val description: String = "",
    val content: String = "",
    val coverImageUrl: String? = null,
    @ServerTimestamp val publishedAt: Date? = null,

    // Fields for bookmark details, only populated for bookmark screen
    var chapter: String? = null,
    var page: Int = 0,
    var progress: Int = 0,
    var snippet: String? = null
)
