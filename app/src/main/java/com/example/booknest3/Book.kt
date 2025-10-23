package com.example.booknest3

import com.google.firebase.firestore.DocumentId

data class Book(
    @DocumentId val id: String = "",
    val title: String = "",
    val author: String = "",
    val description: String = "",
    val coverImageUrl: String? = null,
    // Fields for bookmark details, only populated for bookmark screen
    var chapter: String? = null,
    var page: Int = 0,
    var progress: Int = 0,
    var snippet: String? = null
)
