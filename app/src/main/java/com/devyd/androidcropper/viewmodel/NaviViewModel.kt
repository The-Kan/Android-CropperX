package com.devyd.androidcropper.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.devyd.androidcropper.state.ShowImageState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Stack
import javax.inject.Inject

@HiltViewModel
class NaviViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle) :
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

    fun updateStacksFromShowImageState(state : ShowImageState){
        undoStack = state.undoStack
        redoStack = state.redoStack
    }

    fun reset() {
        undoStack.clear()
        redoStack.clear()
    }

}