package com.example.booknest3

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Data class na kumakatawan sa isang draft book sa Firestore.
 * Ito ang magiging blueprint natin para sa bawat draft document.
 */
data class Draft(
    // Awtomatikong kukunin ng Firestore ang document ID at ilalagay dito.
    @DocumentId
    val id: String = "",

    val title: String = "",
    val content: String = "",

    // Awtomatikong ilalagay ng Firestore server ang petsa kung kailan ito huling binago.
    @ServerTimestamp
    val lastModified: Date? = null
)
