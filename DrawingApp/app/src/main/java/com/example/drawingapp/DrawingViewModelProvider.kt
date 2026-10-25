package com.example.drawingapp

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

object DrawingViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            CanvasViewModel(
                (this[AndroidViewModelFactory.APPLICATION_KEY]
                        as DrawingApplication).drawingRepository
            )
        }
        initializer {
            LibraryViewModel(
                (this[AndroidViewModelFactory.APPLICATION_KEY]
                        as DrawingApplication).drawingRepository
            )
        }
    }
}