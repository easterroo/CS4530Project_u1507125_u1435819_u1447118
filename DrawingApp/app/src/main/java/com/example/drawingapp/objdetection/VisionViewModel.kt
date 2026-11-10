package com.example.drawingapp.objdetection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed interface VisionUiState {
    object Idle : VisionUiState
    object Loading : VisionUiState
    data class Success(
        val imageFile: File,
        val detectedObjects: List<DetectedObject>
    ) : VisionUiState
    data class Error(val message: String) : VisionUiState
}

/**
 * Dedicated ViewModel for Google Vision API object detection.
 * Enforces separation of concerns by safely calling VisionRepository and exposing UI state.
 */
class VisionViewModel(
    private val visionRepository: VisionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<VisionUiState>(VisionUiState.Idle)
    val uiState: StateFlow<VisionUiState> = _uiState.asStateFlow()

    /**
     * Triggers object detection using a file path string.
     */
    fun detectObjects(filePath: String) {
        if (filePath.isBlank()) {
            _uiState.value = VisionUiState.Error("Image file path is empty.")
            return
        }
        val imageFile = File(filePath)

        viewModelScope.launch {
            _uiState.value = VisionUiState.Loading
            val result = visionRepository.detectObjectsInImage(imageFile)
            result.onSuccess { objects ->
                _uiState.value = VisionUiState.Success(
                    imageFile = imageFile,
                    detectedObjects = objects
                )
            }.onFailure { error ->
                _uiState.value = VisionUiState.Error(
                    message = error.localizedMessage ?: "Failed to detect objects."
                )
            }
        }
    }

    /**
     * Resets the UI state back to Idle.
     */
    fun resetState() {
        _uiState.value = VisionUiState.Idle
    }
}
