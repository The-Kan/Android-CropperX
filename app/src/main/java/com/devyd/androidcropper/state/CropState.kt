package com.devyd.androidcropper.state

import android.graphics.Bitmap
import java.util.Stack

data class CropState (
    val undoStack : Stack<Bitmap> = Stack(),
    val redoStack : Stack<Bitmap> = Stack()
)