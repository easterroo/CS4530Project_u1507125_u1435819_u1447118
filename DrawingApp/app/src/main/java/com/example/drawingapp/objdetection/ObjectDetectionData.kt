package com.example.drawingapp.objdetection

/**
 * Normalized bounding box coordinates (values between 0 and 1000).
 */
data class BoundingBox(
    val minX: Int,
    val minY: Int,
    val maxX: Int,
    val maxY: Int
)

/**
 * Domain model representing an object detected by Google Vision API.
 */
data class DetectedObject(
    val label: String,
    val confidenceScore: Float,
    val boundingBox: BoundingBox
)
