package com.devyd.cropperx.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import com.devyd.cropperx.R
import com.devyd.cropperx.crop.CropOptions
import kotlin.math.min

class CropperXView(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    private val cropperImageView: ImageView
    private val cropperControlView: CropperControlView?
    private val progressBar: ProgressBar
    private var originalBitmap: Bitmap? = null
    private var imageResource = 0

    private var mLayoutWidth = 0
    private var mLayoutHeight = 0

    private val mImagePoints = FloatArray(8) // 이미지 변환 시 계산을 위한 8개의 좌표값을 저장하는 배열입니다 (예: 4개의 x,y 좌표쌍).

    var imageUri: Uri? = null
        private set

    private var loadedSampleSize = 1

    private var isCropperControlViewVisible = true

    private val mImageMatrix = Matrix()
    private val mImageInverseMatrix = Matrix()

    internal companion object {
        /**
         * Determines the specs for the onMeasure function. Calculates the width or height depending on
         * the mode.
         *
         * [measureSpecMode] The mode of the measured width or height.
         * [measureSpecSize] The size of the measured width or height.
         * [desiredSize] The desired size of the measured width or height.
         * @return The final size of the width or height.
         */
        internal fun getOnMeasureSpec(
            measureSpecMode: Int,
            measureSpecSize: Int,
            desiredSize: Int,
        ): Int {
            // Measure Width
            return when (measureSpecMode) {
                // MeasureSpec.EXACTLY 자식 뷰(CropImageView)는 부모 뷰가 요구한 "정확한 크기"(match_parent, 100dp) 를 무조건 따라야 합니다.
                MeasureSpec.EXACTLY -> measureSpecSize // Must be this size
                // MeasureSpec.AT_MOST 부모 뷰가 자식 뷰에게 크기 제한을 제공하지만, 자식 뷰는 그 크기 이하로 조정될 수 있음을 의미합니다. 예를 들어, wrap_content
                MeasureSpec.AT_MOST -> min(
                    desiredSize,
                    measureSpecSize,
                ) // Can't be bigger than...; match_parent value
                // UNSPECIFIED는 제한 없음
                else -> desiredSize // Be whatever you want; wrap_content
            }
        }
    }



    init {
        val option = CropOptions()


        val inflater = LayoutInflater.from(context)
        val cropperXView = inflater.inflate(R.layout.cropper_x_view, this, true)
        cropperImageView = cropperXView.findViewById(R.id.CropperImageView)
        cropperControlView = cropperXView.findViewById(R.id.CropperControlView)
        // setCropWindowChangeListener(this) 필요
        cropperControlView.setOption(option)
        progressBar = cropperXView.findViewById(R.id.ProgressBar)
        progressBar.visibility = INVISIBLE
    }


    fun setImageBitmap(bitmap: Bitmap?) {
        // 초기화 단계이므로 크롭뷰도 초기화되어야함.
        setBitmap(bitmap, 0, null, 1)
    }

    private fun setBitmap(
        bitmap: Bitmap?,
        imageResource: Int,
        imageUri: Uri?,
        loadSampleSize: Int
    ) {
        if (originalBitmap == null || originalBitmap != bitmap) {
            clearImage()
            originalBitmap = bitmap
            cropperImageView.setImageBitmap(originalBitmap)
            this.imageResource = imageResource
            this.imageUri = imageUri
            this.loadedSampleSize = loadSampleSize


        }
        // 이미지 확대, 이동시 적용할 메트릭스가 필요.


        // 크롭컨트롤뷰 리셋 필요.
        setCropperControlViewVisible(true)

    }

    private fun clearImage() {
        if (originalBitmap != null && imageResource > 0 || imageUri != null) {
            originalBitmap!!.recycle()
        }

        originalBitmap = null
        imageResource = 0
        imageUri = null
        loadedSampleSize = 1
        cropperImageView.setImageBitmap(null)
        setCropperControlViewVisible(false)
    }

    private fun setCropperControlViewVisible(isVisible: Boolean) {
        if (cropperControlView != null) {
            cropperControlView.visibility = if (isVisible) VISIBLE else INVISIBLE
        }
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec) // 측정 모드를 반환합니다. 예를 들어, EXACTLY, AT_MOST, UNSPECIFIED 등의 값이 있을 수 있습니다.
        val widthSize = MeasureSpec.getSize(widthMeasureSpec) // 뷰가 허용된 최대 크기(또는 원하는 크기)를 반환합니다.
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val bitmap = originalBitmap
        if (bitmap != null) { //  비트맵 이미지가 로드된 상태
            // Bypasses a baffling bug when used within a ScrollView, where heightSize is set to 0.
            if (heightSize == 0) heightSize = bitmap.height
            val desiredWidth: Int
            val desiredHeight: Int
            var viewToBitmapWidthRatio = Double.POSITIVE_INFINITY
            var viewToBitmapHeightRatio = Double.POSITIVE_INFINITY
            // Checks if either width or height needs to be fixed
            if (widthSize < bitmap.width) {
                viewToBitmapWidthRatio = widthSize.toDouble() / bitmap.width.toDouble()
            }
            if (heightSize < bitmap.height) {
                viewToBitmapHeightRatio = heightSize.toDouble() / bitmap.height.toDouble()
            }
            // If either needs to be fixed, choose the smallest ratio and calculate from there
            if (viewToBitmapWidthRatio != Double.POSITIVE_INFINITY ||
                viewToBitmapHeightRatio != Double.POSITIVE_INFINITY
            ) {
                // 세로의 비율이 더 커서 여유가 있는 경우, 가로를 가득채우고 세로를 여유롭게 채운다.
                if (viewToBitmapWidthRatio <= viewToBitmapHeightRatio) {
                    desiredWidth = widthSize
                    desiredHeight = (bitmap.height * viewToBitmapWidthRatio).toInt()
                } else { // 가로의 비율이 더 커서 여유가 있는 경우, 세로를 가득채우고 가로를 여유롭게 채운다.
                    desiredHeight = heightSize
                    desiredWidth = (bitmap.width * viewToBitmapHeightRatio).toInt()
                }
            } else {
                // Otherwise, the picture is within frame layout bounds. Desired width is simply picture
                // size
                desiredWidth = bitmap.width
                desiredHeight = bitmap.height
            }
            val width = getOnMeasureSpec(widthMode, widthSize, desiredWidth)
            val height = getOnMeasureSpec(heightMode, heightSize, desiredHeight)
            mLayoutWidth = width
            mLayoutHeight = height
            setMeasuredDimension(mLayoutWidth, mLayoutHeight)
        } else {
            setMeasuredDimension(widthSize, heightSize)
        }


    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (mLayoutWidth > 0 && mLayoutHeight > 0) {
            if (originalBitmap != null) {

                applyImageMatrix((right-left).toFloat(), (bottom-top).toFloat())

                updateImageBounds()
            }
        }
    }

    private fun applyImageMatrix(width: Float, height: Float) {
        val bitmap = originalBitmap
        if (bitmap != null && width > 0 && height > 0) {
            mImageMatrix.invert(mImageInverseMatrix)

            val cropRect = cropperControlView!!.cropWindowRect

            mImageInverseMatrix.mapRect(cropRect)

            mImageMatrix.reset()
            mImageMatrix.postTranslate(
                // (width - bitmap.width) / 2: 이미지가 중앙에 오도록 가로 방향으로 이동
                // (height - bitmap.height) / 2: 이미지가 중앙에 오도록 세로 방향으로 이동
                (width - bitmap.width) / 2,
                (height - bitmap.height) / 2,
            )
            mapImagePointsByImageMatrix()
        }
    }

    private fun mapImagePointsByImageMatrix() {
        // 좌상단 (0, 0)
        mImagePoints[0] = 0f
        mImagePoints[1] = 0f
        // 우상단 (w, 0)
        mImagePoints[2] = originalBitmap!!.width.toFloat()
        mImagePoints[3] = 0f
        // 우하단 (w, h)
        mImagePoints[4] = originalBitmap!!.width.toFloat()
        mImagePoints[5] = originalBitmap!!.height.toFloat()
        // 좌하단 (0, h)
        mImagePoints[6] = 0f
        mImagePoints[7] = originalBitmap!!.height.toFloat()
        // mapPoints()는 해당 매트릭스를 mImagePoints에 적용해서, 변환된 좌표를 직접 mImagePoints 배열에 덮어씌웁니다.
        // x,y 모두 150씩 이동시킨 네 꼭지점을 mImagePoints에 저장하라와 같은 동작.
        mImageMatrix.mapPoints(mImagePoints)
    }

    private fun updateImageBounds() {
        // 크롭 오버레이의 사각형 위치 갱신

        if(originalBitmap != null){

            //현재 View의 너비, 높이, 그리고 스케일 비율을 넘겨줌
            //이를 통해 크롭 가능한 영역의 최대치(=제한 범위)를 설정
            cropperControlView!!.setCropWindowLimits(
                width.toFloat(),
                height.toFloat(),
            )
        }

        cropperControlView!!.setBounds(mImagePoints, width, height)
    }
}

