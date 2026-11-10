package com.example.drawingapp.objdetection

import kotlinx.serialization.Serializable

/**
 * Normalized bounding box coordinates (values between 0f and 1f).
 */
data class BoundingBox(
    val minX: Float,
    val minY: Float,
    val maxX: Float,
    val maxY: Float
)

/**
 * Domain model representing an object detected by Google Vision API.
 */
data class DetectedObject(
    val name: String,
    val category: String,
    val confidence: Float,
    val boundingBox: BoundingBox
)

// Internal DTOs for Google Vision API (OBJECT_LOCALIZATION feature)

@Serializable
internal data class VisionApiRequest(
    val requests: List<AnnotateImageRequest>
)

@Serializable
internal data class AnnotateImageRequest(
    val image: ImagePayload,
    val features: List<FeaturePayload>
)

@Serializable
internal data class ImagePayload(
    val content: String
)

@Serializable
internal data class FeaturePayload(
    val type: String = "OBJECT_LOCALIZATION",
    val maxResults: Int = 10
)

@Serializable
internal data class VisionApiResponse(
    val responses: List<AnnotateImageResponse>? = null
)

@Serializable
internal data class AnnotateImageResponse(
    val localizedObjectAnnotations: List<LocalizedObjectAnnotation>? = null,
    val error: StatusPayload? = null
)

@Serializable
internal data class LocalizedObjectAnnotation(
    val mid: String? = null,
    val name: String? = null,
    val score: Float? = null,
    val boundingPoly: BoundingPolyPayload? = null
)

@Serializable
internal data class BoundingPolyPayload(
    val normalizedVertices: List<NormalizedVertexPayload>? = null
)

@Serializable
internal data class NormalizedVertexPayload(
    val x: Float? = 0f,
    val y: Float? = 0f
)

@Serializable
internal data class StatusPayload(
    val code: Int? = null,
    val message: String? = null
)
