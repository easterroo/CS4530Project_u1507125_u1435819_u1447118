package com.example.drawingapp.objdetection

import android.graphics.BitmapFactory
import com.example.drawingapp.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.FunctionType
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Repository responsible for interacting with the Google Vision API to detect objects in image files.
 */
open class ObjectDetectionRepository() {
    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-3.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = Schema(
                    name = "DetectedObjectList",
                    type = FunctionType.ARRAY,
                    description = "List of detected objects",
                    items = Schema(
                        name = "DetectedObject",
                        type = FunctionType.OBJECT,
                        properties = mapOf(
                            "label" to Schema(
                                name = "Label",
                                type = FunctionType.STRING,
                                description = "The name of the object"
                            ),
                            "confidenceScore" to Schema(
                                name = "ConfidenceScore",
                                type = FunctionType.NUMBER,
                                description = "Confidence score of the object"
                            ),
                            "boundingBox" to Schema(
                                name = "BoundingBox",
                                type = FunctionType.ARRAY,
                                items = Schema(
                                    name = "Coordinate",
                                    type = FunctionType.INTEGER,
                                    description = "A value on the x or y axis"
                                ),
                                description = "Bounding box coordinates [xmin, ymin, xmax, ymax]"
                            )
                        ),
                        required = listOf("label", "confidenceScore", "boundingBox"),
                        description = ""
                    )
                )
            }
        )
    }

    /**
     * Sends an image file from the drawings folder to Google Vision API for object detection.
     *
     * Returns a [Result] containing a list of [DetectedObject] (label, confidence score, and bounding box coordinates).
     */
    open suspend fun detectObjectsInImage(imageFile: File): Result<List<DetectedObject>> = withContext(Dispatchers.IO) {
        runCatching {
            if (model.apiKey.isBlank()) {
                throw IllegalArgumentException("Vision API key is not configured in secrets.properties.")
            }
            if (!imageFile.exists() || !imageFile.isFile) {
                throw IllegalArgumentException("Image file does not exist at path: ${imageFile.absolutePath}")
            }

            val imageBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)

            val prompt = """
                Locate the main objects in this image. 
                For each object, provide its label, confidence score, and bounding box coordinates 
                normalized to a 0-1000 scale in the format: [xmin, ymin, xmax, ymax].
                Return the output strictly as a JSON list. 
                Example: [{"label": "dog", "confidenceScore": 99.5, "boundingBox": [100, 200, 500, 800]}]
            """.trimIndent()

            val inputContent = content {
                image(imageBitmap)
                text(prompt)
            }

            return@runCatching parseResponse(model.generateContent(inputContent).text)
        }
    }

    private fun parseResponse(rawResponse: String?): List<DetectedObject> {
        if (rawResponse.isNullOrBlank()) return emptyList()

        // Clean up markdown text formatting if Gemini wraps it in ```json ... ```
        val cleanJson = rawResponse
            .replace("```json", "")
            .replace("```", "")
            .trim()

        // Safely parse the JSON string into Kotlin objects
        return try {
            val jsonConfig = Json {
                ignoreUnknownKeys = true // Prevents crashes if Gemini adds extra fields
                coerceInputValues = true
            }
            jsonConfig.decodeFromString<List<DetectedObject>>(cleanJson)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // Return empty list if formatting failed
        }
    }
}
