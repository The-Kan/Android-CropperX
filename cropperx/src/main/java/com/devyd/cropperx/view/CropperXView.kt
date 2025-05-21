package com.devyd.cropperx.view

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import com.devyd.cropperx.R

class CropperXView (context : Context, attrs : AttributeSet? = null) : FrameLayout(context, attrs){

    private val cropperImageView: ImageView
    private val cropperControlView: CropperControlView
    private val progressBar : ProgressBar
    private var originalBitmap: Bitmap? = null
    private var imageResource = 0

    var imageUri: Uri? = null
        private set

    private var loadedSampleSize = 1

    private var isCropperControlViewVisible = true


    init {
        val inflater = LayoutInflater.from(context)
        val cropperXView = inflater.inflate(R.layout.cropper_x_view, this, true)
        cropperImageView = cropperXView.findViewById(R.id.CropperImageView)
        cropperControlView = cropperXView.findViewById(R.id.CropperControlView)
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
        if(originalBitmap == null || originalBitmap != bitmap){
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
        if(originalBitmap != null && imageResource > 0 || imageUri != null) {
            originalBitmap!!.recycle()
        }

        originalBitmap = null
        imageResource = 0
        imageUri = null
        loadedSampleSize = 1
        cropperImageView.setImageBitmap(null)
        setCropperControlViewVisible(false)
    }

    private fun setCropperControlViewVisible(isVisible : Boolean) {
        if(cropperControlView != null){
            cropperImageView.visibility = if(isVisible) VISIBLE else INVISIBLE
        }

    }

}