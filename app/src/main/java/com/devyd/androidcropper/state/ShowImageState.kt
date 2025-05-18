package com.devyd.androidcropper.state

import android.graphics.Bitmap
import androidx.compose.runtime.Stable
import java.util.Stack

@Stable
data class ShowImageState (
    val undoStack : Stack<Bitmap> = Stack(),
    val redoStack : Stack<Bitmap> = Stack(),
    val recompositionTrigger : Int = 0
)