package com.devyd.androidcropper.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Stack
import javax.inject.Inject

@HiltViewModel
class CropViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle) :
    ViewModel() {

    var undoStack: Stack<Bitmap> = Stack()
        private set
    var redoStack: Stack<Bitmap> = Stack()
        private set


    fun addToStack(
        bitmap: Bitmap
    ) {
        undoStack.push(bitmap)
    }

}