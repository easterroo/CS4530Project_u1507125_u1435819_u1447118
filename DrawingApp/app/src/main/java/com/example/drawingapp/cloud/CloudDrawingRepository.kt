package com.example.drawingapp.cloud

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import kotlin.coroutines.resume

/**
 * Repository for interacting with Firebase Storage and Firestore for Cloud Backup and Sync.
 */
class CloudDrawingRepository(
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (_: Exception) { null },
    private val storage: FirebaseStorage? = try { FirebaseStorage.getInstance() } catch (_: Exception) { null }
) {

    private fun getStorageInstance(): FirebaseStorage {
        storage?.let { return it }
        return try {
            FirebaseStorage.getInstance()
        } catch (_: Exception) {
            try {
                FirebaseStorage.getInstance("gs://drawingapp-7f34b.firebasestorage.app")
            } catch (_: Exception) {
                FirebaseStorage.getInstance("gs://drawingapp-7f34b.appspot.com")
            }
        }
    }

    private fun getStorageReference(firebaseStorage: FirebaseStorage, path: String): StorageReference {
        return try {
            firebaseStorage.reference.child(path)
        } catch (_: Exception) {
            try {
                firebaseStorage.getReferenceFromUrl("gs://drawingapp-7f34b.firebasestorage.app").child(path)
            } catch (_: Exception) {
                firebaseStorage.getReferenceFromUrl("gs://drawingapp-7f34b.appspot.com").child(path)
            }
        }
    }

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
            if (!imageFile.exists() || imageFile.length() == 0L) {
                throw IOException("Image file does not exist or is empty: ${imageFile.absolutePath}")
            }

            val firebaseFirestore = firestore ?: try { FirebaseFirestore.getInstance() } catch (_: Exception) { throw IllegalStateException("Firestore is not initialized.") }
            val firebaseStorage = getStorageInstance()

            val fileName = "drawing_${drawingId}_${System.currentTimeMillis()}.png"
            val storageRef = getStorageReference(firebaseStorage, "drawings/$userId/$fileName")

            val bytes = imageFile.readBytes()
            val metadata = StorageMetadata.Builder()
                .setContentType("image/png")
                .build()

            // 1. Upload image data to Firebase Storage (try putBytes first, fallback to putFile)
            var uploadError: Throwable? = null
            var uploadSuccess = false

            try {
                suspendCancellableCoroutine<Unit> { continuation ->
                    val uploadTask = storageRef.putBytes(bytes, metadata)
                    uploadTask.addOnSuccessListener {
                        if (continuation.isActive) continuation.resume(Unit)
                    }.addOnFailureListener { exception ->
                        if (continuation.isActive) continuation.resumeWith(Result.failure(exception))
                    }
                    continuation.invokeOnCancellation {
                        try { uploadTask.cancel() } catch (_: Exception) {}
                    }
                }
                uploadSuccess = true
            } catch (e: Exception) {
                uploadError = e
            }

            if (!uploadSuccess) {
                // Fallback attempt: putFile with explicit metadata
                try {
                    suspendCancellableCoroutine<Unit> { continuation ->
                        val uploadTask = storageRef.putFile(Uri.fromFile(imageFile), metadata)
                        uploadTask.addOnSuccessListener {
                            if (continuation.isActive) continuation.resume(Unit)
                        }.addOnFailureListener { exception ->
                            if (continuation.isActive) continuation.resumeWith(Result.failure(exception))
                        }
                        continuation.invokeOnCancellation {
                            try { uploadTask.cancel() } catch (_: Exception) {}
                        }
                    }
                    uploadSuccess = true
                } catch (e: Exception) {
                    uploadError = e
                }
            }

            if (!uploadSuccess && uploadError != null) {
                throw uploadError
            }

            // 2. Get download URL
            val downloadUrl = suspendCancellableCoroutine<Uri> { continuation ->
                val task = storageRef.downloadUrl
                task.addOnSuccessListener { uri ->
                    if (continuation.isActive) continuation.resume(uri)
                }.addOnFailureListener { exception ->
                    if (continuation.isActive) continuation.resumeWith(Result.failure(exception))
                }
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
                    .addOnSuccessListener {
                        if (continuation.isActive) continuation.resume(Unit)
                    }
                    .addOnFailureListener { exception ->
                        if (continuation.isActive) continuation.resumeWith(Result.failure(exception))
                    }
            }

            cloudData
        }
    }

    /**
     * Queries Firestore for all documents in "user_drawings" matching [userId].
     */
    suspend fun fetchUserDrawings(userId: String): Result<List<CloudDrawingData>> = withContext(Dispatchers.IO) {
        runCatching {
            val firebaseFirestore = firestore ?: try { FirebaseFirestore.getInstance() } catch (_: Exception) { throw IllegalStateException("Firestore is not initialized.") }
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
                        if (continuation.isActive) continuation.resume(resultList)
                    }
                    .addOnFailureListener { exception ->
                        if (continuation.isActive) continuation.resumeWith(Result.failure(exception))
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
            val firebaseStorage = getStorageInstance()
            val storageRef = firebaseStorage.getReferenceFromUrl(imageUrl)
            suspendCancellableCoroutine { continuation ->
                val task = storageRef.getFile(targetFile)
                task.addOnSuccessListener {
                    if (continuation.isActive) continuation.resume(Unit)
                }.addOnFailureListener { exception ->
                    if (continuation.isActive) continuation.resumeWith(Result.failure(exception))
                }
                continuation.invokeOnCancellation {
                    try { task.cancel() } catch (_: Exception) {}
                }
            }
        }
    }
}
