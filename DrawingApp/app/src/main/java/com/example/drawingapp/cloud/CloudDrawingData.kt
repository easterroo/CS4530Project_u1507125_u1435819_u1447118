package com.example.drawingapp.cloud

import com.google.firebase.firestore.DocumentId

/**
 * Data model for drawing metadata stored in Firestore.
 */
data class CloudDrawingData(
    @DocumentId val docId: String = "",
    val drawingId: Int = 0,
    val userId: String = "",
    val title: String = "",
    val imageUrl: String = "",
    val timestamp: Long = 0L,
    val width: Float = 1000f,
    val height: Float = 1000f
)
