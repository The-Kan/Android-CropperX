package com.devyd.cropperx.crop

import android.graphics.RectF

internal class CropRectController {


    private val cropRect = RectF()
    private val readCropRect = RectF()


    fun getRect() : RectF {
        readCropRect.set(cropRect)
        return readCropRect
    }
}