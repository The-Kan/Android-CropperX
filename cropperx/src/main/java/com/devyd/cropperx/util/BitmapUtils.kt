package com.devyd.cropperx.util

import android.graphics.RectF
import kotlin.math.max
import kotlin.math.min

internal object BitmapUtils {

    val EMPTY_RECT_F = RectF()

    fun getRectLeft(points: FloatArray): Float = min(min(min(points[0], points[2]), points[4]), points[6])

    fun getRectTop(points: FloatArray): Float = min(min(min(points[1], points[3]), points[5]), points[7])

    fun getRectRight(points: FloatArray): Float = max(max(max(points[0], points[2]), points[4]), points[6])

    fun getRectBottom(points: FloatArray): Float = max(max(max(points[1], points[3]), points[5]), points[7])

    fun getRectWidth(points: FloatArray): Float = getRectRight(points) - getRectLeft(points)

    fun getRectHeight(points: FloatArray): Float = getRectBottom(points) - getRectTop(points)

    fun getRectCenterX(points: FloatArray): Float = (getRectRight(points) + getRectLeft(points)) / 2f

    fun getRectCenterY(points: FloatArray): Float = (getRectBottom(points) + getRectTop(points)) / 2f
}