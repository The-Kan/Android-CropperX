package com.devyd.androidcropper.bitmap

import android.graphics.Bitmap

sealed class BitmapStatus {
    data object None: BitmapStatus()
    data object Decoding: BitmapStatus()
    data class Success(val bitmap: Bitmap): BitmapStatus()
    data class Fail(val message: String? = null): BitmapStatus()
}