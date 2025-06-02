package com.devyd.cropperx.crop

import android.graphics.RectF
import android.util.Log
import com.devyd.cropperx.view.CropShape
import kotlin.math.abs
import kotlin.math.max

internal class CropRectController {


    private val cropRect = RectF()
    private val readCropRect = RectF()


    fun getRect(): RectF {
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
        scaleFactorWidth: Float,
        scaleFactorHeight: Float,
    ) {
        mMaxCropWindowWidth = maxWidth
        mMaxCropWindowHeight = maxHeight
        mScaleFactorWidth = scaleFactorWidth
        mScaleFactorHeight = scaleFactorHeight
    }

    fun getMoveHandler(
        x: Float,
        y: Float,
        targetRadius: Float,
        cropShape: CropShape,
        isCenterMoveEnabled: Boolean,
    ): CropRectMoveController? {
        val type: CropRectMoveController.Type? = when (cropShape) {
            CropShape.RECTANGLE -> getRectanglePressedMoveType(
                x, y, targetRadius, isCenterMoveEnabled
            )
        }

        return if (type != null) CropRectMoveController(type, this, x, y) else null
    }

    private fun getRectanglePressedMoveType(
        x: Float,
        y: Float,
        targetRadius: Float,
        isCenterMoveEnabled: Boolean,
    ): CropRectMoveController.Type? {

        return when {
            isInCornerTargetZone(x, y, cropRect.left, cropRect.top, targetRadius) -> {
                Log.i("Deok", "CROP RECT의 TOP_LEFT를 터치함")
                CropRectMoveController.Type.TOP_LEFT
            }

            isInCornerTargetZone(x, y, cropRect.right, cropRect.top, targetRadius) -> {
                Log.i("Deok", "CROP RECT의 TOP_RIGHT 터치함")
                CropRectMoveController.Type.TOP_RIGHT
            }

            isInCornerTargetZone(x, y, cropRect.left, cropRect.bottom, targetRadius) -> {
                Log.i("Deok", "CROP RECT의 BOTTOM_LEFT 터치함")
                CropRectMoveController.Type.BOTTOM_LEFT
            }

            isInCornerTargetZone(x, y, cropRect.right, cropRect.bottom, targetRadius) -> {
                Log.i("Deok", "CROP RECT의 BOTTOM_RIGHT 터치함")
                CropRectMoveController.Type.BOTTOM_RIGHT
            }

            isCenterMoveEnabled && isInCenterTargetZone(
                x, y, cropRect.left, cropRect.top, cropRect.right, cropRect.bottom
            ) && focusCenter() -> {
                Log.i("Deok", "CROP RECT의 CENTER를 터치함(크롭이 작을때)")
                CropRectMoveController.Type.CENTER
            }

            isInHorizontalTargetZone(
                x, y, cropRect.left, cropRect.right, cropRect.top, targetRadius
            ) -> {
                Log.i("Deok", "CROP RECT의 TOP 터치함")
                CropRectMoveController.Type.TOP
            }

            isInHorizontalTargetZone(
                x, y, cropRect.left, cropRect.right, cropRect.bottom, targetRadius
            ) -> {
                Log.i("Deok", "CROP RECT의 BOTTOM 터치함")
                CropRectMoveController.Type.BOTTOM
            }

            isInVerticalTargetZone(
                x, y, cropRect.left, cropRect.top, cropRect.bottom, targetRadius
            ) -> {
                Log.i("Deok", "CROP RECT의 LEFT 터치함")
                CropRectMoveController.Type.LEFT
            }

            isInVerticalTargetZone(
                x, y, cropRect.right, cropRect.top, cropRect.bottom, targetRadius
            ) -> {
                Log.i("Deok", "CROP RECT의 RIGHT 터치함")
                CropRectMoveController.Type.RIGHT
            }

            isCenterMoveEnabled && isInCenterTargetZone(
                x, y, cropRect.left, cropRect.top, cropRect.right, cropRect.bottom
            ) && !focusCenter() -> {
                Log.i("Deok", "CROP RECT의 CENTER 터치함")
                CropRectMoveController.Type.CENTER
            }

            else -> {
                Log.i("Deok", "getOutSideOfCenter()")
                getOutSideOfCenter(x,y,isCenterMoveEnabled)
            }
        }
    }

    private fun getOutSideOfCenter(
        x: Float,
        y: Float,
        isCenterMoveEnabled: Boolean,
    ): CropRectMoveController.Type? {

        val cellLength = cropRect.width() / 6
        val leftCenter = cropRect.left + cellLength
        val rightCenter = cropRect.left + 5 * cellLength
        val cellHeight = cropRect.height() / 6
        val topCenter = cropRect.top + cellHeight
        val bottomCenter = cropRect.top + 5 * cellHeight
        return when {
            x < leftCenter -> {
                when {
                    y < topCenter -> CropRectMoveController.Type.TOP_LEFT
                    y < bottomCenter -> CropRectMoveController.Type.LEFT
                    else -> CropRectMoveController.Type.BOTTOM_LEFT
                }
            }
            x < rightCenter -> {
                when {
                    y < topCenter -> CropRectMoveController.Type.TOP
                    y < bottomCenter -> if (isCenterMoveEnabled) {
                        CropRectMoveController.Type.CENTER
                    } else {
                        null
                    }
                    else -> CropRectMoveController.Type.BOTTOM
                }
            }
            else -> {
                when {
                    y < topCenter -> CropRectMoveController.Type.TOP_RIGHT
                    y < bottomCenter -> CropRectMoveController.Type.RIGHT
                    else -> CropRectMoveController.Type.BOTTOM_RIGHT
                }
            }
        }
    }

    private fun focusCenter() = (cropRect.width() < 100 || cropRect.height() < 100)

    private fun isInCenterTargetZone(
        x: Float,
        y: Float,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ) = x > left && x < right && y > top && y < bottom


    private fun isInCornerTargetZone(
        x: Float,
        y: Float,
        handleX: Float,
        handleY: Float,
        targetRadius: Float,
    ) = distance(x, y, handleX, handleY) <= targetRadius

    private fun isInHorizontalTargetZone(
        x: Float,
        y: Float,
        handleXStart: Float,
        handleXEnd: Float,
        handleY: Float,
        targetRadius: Float,
    ) = x > handleXStart && x < handleXEnd && abs(y - handleY) <= targetRadius

    private fun isInVerticalTargetZone(
        x: Float,
        y: Float,
        handleX: Float,
        handleYStart: Float,
        handleYEnd: Float,
        targetRadius: Float,
    ) = abs(x - handleX) <= targetRadius && y > handleYStart && y < handleYEnd

    private fun distance(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
    ) = max(abs(x1 - x2), abs(y1 - y2))
}