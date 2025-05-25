package com.devyd.cropperx.crop

import android.graphics.RectF

internal class CropRectController {


    private val cropRect = RectF()
    private val readCropRect = RectF()


    fun getRect() : RectF {
        readCropRect.set(cropRect)
        return readCropRect
    }

    private var mScaleFactorWidth = 1f

    fun getScaleFactorWidth() = mScaleFactorWidth

    private var mScaleFactorHeight = 1f

    fun getScaleFactorHeight() = mScaleFactorHeight


    // 뷰(View) 좌표계에서 정해둔 “크롭 박스 자체의 최소 너비” (예: 사용자가 너무 작게 조절하지 못하도록 제한하는 값)
    private var mMinCropWindowWidth = 0f

    // mMinCropResultWidth는 “크롭 결과(원본 비트맵)에서 보장해야 할 최소 픽셀 너비”
    private var mMinCropResultWidth = 0f

    // 뷰에서 허용할 최소 크롭 너비
    fun getMinCropWidth() =
        mMinCropWindowWidth.coerceAtLeast(mMinCropResultWidth / mScaleFactorWidth)


    private var mMinCropWindowHeight = 0f

    private var mMinCropResultHeight = 0f

    fun getMinCropHeight() =
        mMinCropWindowHeight.coerceAtLeast(mMinCropResultHeight / mScaleFactorHeight)

    private var mMaxCropWindowWidth = 0f
    private var mMaxCropResultWidth = 0f

    fun getMaxCropWidth() =
        mMaxCropWindowWidth.coerceAtMost(mMaxCropResultWidth / mScaleFactorWidth)


    private var mMaxCropWindowHeight = 0f
    private var mMaxCropResultHeight = 0f

    fun getMaxCropHeight() =
        mMaxCropWindowHeight.coerceAtMost(mMaxCropResultHeight / mScaleFactorHeight)

    fun setRect(rect: RectF) {
        cropRect.set(rect)
    }

    fun setInitialAttributeValues(options: CropOptions) {
        mMinCropWindowWidth = options.minCropWindowWidth.toFloat()
        mMinCropWindowHeight = options.minCropWindowHeight.toFloat()
        mMinCropResultWidth = options.minCropResultWidth.toFloat()
        mMinCropResultHeight = options.minCropResultHeight.toFloat()
        mMaxCropResultWidth = options.maxCropResultWidth.toFloat()
        mMaxCropResultHeight = options.maxCropResultHeight.toFloat()
    }

    fun setMinCropResultSize(minCropResultWidth: Int, minCropResultHeight: Int) {
        mMinCropResultWidth = minCropResultWidth.toFloat()
        mMinCropResultHeight = minCropResultHeight.toFloat()
    }

    fun setMaxCropResultSize(maxCropResultWidth: Int, maxCropResultHeight: Int) {
        mMaxCropResultWidth = maxCropResultWidth.toFloat()
        mMaxCropResultHeight = maxCropResultHeight.toFloat()
    }

    fun setCropWindowLimits(
        maxWidth: Float,
        maxHeight: Float,
    ) {
        mMaxCropWindowWidth = maxWidth
        mMaxCropWindowHeight = maxHeight
    }

}