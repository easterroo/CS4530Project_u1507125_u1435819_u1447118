package com.example.drawingapp.objdetection

import android.util.Base64
import com.example.drawingapp.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Repository responsible for interacting with the Google Vision API to detect objects in image files.
 */
open class VisionRepository(
    private val apiKey: String = BuildConfig.VISION_API_KEY,
    private val client: HttpClient = defaultHttpClient
) {

    companion object {
        private val defaultHttpClient by lazy {
            HttpClient(Android) {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        encodeDefaults = true
                    })
                }
            }
        }
    }

    /**
     * Sends an image file from the drawings folder to Google Vision API for object detection.
     *
     * Returns a [Result] containing a list of [DetectedObject] (name, category, confidence score, and bounding box coordinates).
     */
    open suspend fun detectObjectsInImage(imageFile: File): Result<List<DetectedObject>> = withContext(Dispatchers.IO) {
        runCatching {
            if (apiKey.isBlank() || apiKey == "YOUR_GOOGLE_VISION_API_KEY_HERE") {
                throw IllegalArgumentException("Vision API key is not configured in secrets.properties.")
            }
            if (!imageFile.exists() || !imageFile.isFile) {
                throw IllegalArgumentException("Image file does not exist at path: ${imageFile.absolutePath}")
            }

            val imageBytes = imageFile.readBytes()
            if (imageBytes.isEmpty()) {
                throw IllegalArgumentException("Image file is empty.")
            }

            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

            val requestPayload = VisionApiRequest(
                requests = listOf(
                    AnnotateImageRequest(
                        image = ImagePayload(content = base64Image),
                        features = listOf(
                            FeaturePayload(type = "OBJECT_LOCALIZATION", maxResults = 10)
                        )
                    )
                )
            )

            val response: VisionApiResponse = client.post("https://vision.googleapis.com/v1/images:annotate?key=$apiKey") {
                contentType(ContentType.Application.Json)
                setBody(requestPayload)
            }.body()

            val annotateResponse = response.responses?.firstOrNull()
            if (annotateResponse?.error != null) {
                throw IllegalStateException("Google Vision API error: ${annotateResponse.error.message}")
            }

            val annotations = annotateResponse?.localizedObjectAnnotations ?: emptyList()

            annotations.map { annotation ->
                val vertices = annotation.boundingPoly?.normalizedVertices ?: emptyList()
                val minX = vertices.minOfOrNull { it.x ?: 0f } ?: 0f
                val minY = vertices.minOfOrNull { it.y ?: 0f } ?: 0f
                val maxX = vertices.maxOfOrNull { it.x ?: 0f } ?: 0f
                val maxY = vertices.maxOfOrNull { it.y ?: 0f } ?: 0f

                DetectedObject(
                    name = annotation.name ?: "Unknown Object",
                    category = annotation.mid ?: "General",
                    confidence = annotation.score ?: 0f,
                    boundingBox = BoundingBox(
                        minX = minX,
                        minY = minY,
                        maxX = maxX,
                        maxY = maxY
                    )
                )
            }
        }
    }
}
