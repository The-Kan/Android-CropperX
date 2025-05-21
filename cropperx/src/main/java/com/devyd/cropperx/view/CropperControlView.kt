package com.devyd.cropperx.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.devyd.cropperx.crop.CropRectController

class CropperControlView (context : Context, attrs : AttributeSet?) : View(context, attrs) {


    private var borderPaint : Paint? = null
    private var cropRectController = CropRectController()

    var cropShape : CropShape? = null
        private set


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawBorder(canvas)

    }

    private fun drawBorder(canvas: Canvas) {
        if(borderPaint != null){
            val w = borderPaint!!.strokeWidth
            val rect = cropRectController.getRect()
            // 안쪽으로 w/2만큼 땅김.
            rect.inset(w/2, w/2)

            when(cropShape){
                CropShape.RECTANGLE ->
                    canvas.drawRect(rect, borderPaint!!)
                else -> throw IllegalStateException("Unknown Crop Shape")
            }
        }
    }
}

enum class CropShape {
    RECTANGLE,
}