package com.devyd.androidcropper.viewmodel

import android.graphics.Bitmap
import android.net.Uri
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

    var inSampleSize : Int = 0
        private set

    var originalImageUri : Uri? = null
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

    fun getCurrentBitmap() : Bitmap {
        val bitmapStack = undoStack
        if(bitmapStack.isEmpty()){
            // 빈 비트맵을 리턴? 또는 에러처리.
        }
        return bitmapStack.peek()
    }

    fun setInSampleSize(size : Int){
        inSampleSize = size
    }

    fun setOriginalImageUri(uri : Uri){
        originalImageUri = uri
    }

}