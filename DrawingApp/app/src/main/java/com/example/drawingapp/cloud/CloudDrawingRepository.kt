package com.example.drawingapp.cloud

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

/**
 * Repository for interacting with Firebase Storage and Firestore for Cloud Backup and Sync.
 */
class CloudDrawingRepository(
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (_: Exception) { null },
    private val storage: FirebaseStorage? = try { FirebaseStorage.getInstance() } catch (_: Exception) { null }
) {
    /**
     * Uploads drawing image to Firebase Storage and metadata to Firestore collection "user_drawings".
     */
    suspend fun uploadDrawing(
        userId: String,
        drawingId: Int,
        title: String,
        width: Float,
        height: Float,
        imageFile: File
    ): Result<CloudDrawingData> = withContext(Dispatchers.IO) {
        runCatching {
            val firebaseFirestore = firestore ?: throw IllegalStateException("Firestore is not initialized.")
            val firebaseStorage = storage ?: throw IllegalStateException("FirebaseStorage is not initialized.")

            val fileName = "drawing_${drawingId}_${System.currentTimeMillis()}.png"
            val storageRef = firebaseStorage.reference.child("drawings/$userId/$fileName")

            // 1. Upload image file to Firebase Storage
            suspendCancellableCoroutine<Unit> { continuation ->
                storageRef.putFile(Uri.fromFile(imageFile))
                    .addOnSuccessListener { continuation.resume(Unit) }
                    .addOnFailureListener { exception -> continuation.resumeWith(Result.failure(exception)) }
            }

            // 2. Get download URL
            val downloadUrl = suspendCancellableCoroutine<Uri> { continuation ->
                storageRef.downloadUrl
                    .addOnSuccessListener { uri -> continuation.resume(uri) }
                    .addOnFailureListener { exception -> continuation.resumeWith(Result.failure(exception)) }
            }

            val docId = "${userId}_$drawingId"
            val timestamp = System.currentTimeMillis()
            val cloudData = CloudDrawingData(
                docId = docId,
                drawingId = drawingId,
                userId = userId,
                title = title.ifBlank { "Untitled Drawing" },
                imageUrl = downloadUrl.toString(),
                timestamp = timestamp,
                width = width,
                height = height
            )

            val firestoreMap = mapOf(
                "drawingId" to drawingId,
                "userId" to userId,
                "title" to cloudData.title,
                "imageUrl" to cloudData.imageUrl,
                "timestamp" to timestamp,
                "width" to width,
                "height" to height
            )

            // 3. Store metadata in Firestore collection "user_drawings"
            suspendCancellableCoroutine<Unit> { continuation ->
                firebaseFirestore.collection("user_drawings")
                    .document(docId)
                    .set(firestoreMap)
                    .addOnSuccessListener { continuation.resume(Unit) }
                    .addOnFailureListener { exception -> continuation.resumeWith(Result.failure(exception)) }
            }

            cloudData
        }
    }

    /**
     * Queries Firestore for all documents in "user_drawings" matching [userId].
     */
    suspend fun fetchUserDrawings(userId: String): Result<List<CloudDrawingData>> = withContext(Dispatchers.IO) {
        runCatching {
            val firebaseFirestore = firestore ?: throw IllegalStateException("Firestore is not initialized.")
            suspendCancellableCoroutine { continuation ->
                firebaseFirestore.collection("user_drawings")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val resultList = querySnapshot.documents.map { doc ->
                            CloudDrawingData(
                                docId = doc.id,
                                drawingId = (doc.getLong("drawingId") ?: 0L).toInt(),
                                userId = doc.getString("userId") ?: userId,
                                title = doc.getString("title") ?: "Untitled Drawing",
                                imageUrl = doc.getString("imageUrl") ?: "",
                                timestamp = doc.getLong("timestamp") ?: 0L,
                                width = (doc.getDouble("width") ?: 1000.0).toFloat(),
                                height = (doc.getDouble("height") ?: 1000.0).toFloat()
                            )
                        }
                        continuation.resume(resultList)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWith(Result.failure(exception))
                    }
            }
        }
    }

    /**
     * Downloads an image from Firebase Storage to a local file.
     */
    suspend fun downloadImageToFile(imageUrl: String, targetFile: File): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (imageUrl.isBlank()) return@runCatching
            val firebaseStorage = storage ?: throw IllegalStateException("FirebaseStorage is not initialized.")
            val storageRef = firebaseStorage.getReferenceFromUrl(imageUrl)
            suspendCancellableCoroutine { continuation ->
                storageRef.getFile(targetFile)
                    .addOnSuccessListener { continuation.resume(Unit) }
                    .addOnFailureListener { exception -> continuation.resumeWith(Result.failure(exception)) }
            }
        }
    }
}
