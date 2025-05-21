package com.devyd.cropperx

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar

class CropperXView (context : Context, attrs : AttributeSet? = null) : FrameLayout(context, attrs){

    private val cropperImageView: ImageView
    private val cropperControlView: ImageView
    private val progressBar : ProgressBar

    init {
        val inflater = LayoutInflater.from(context)
        val cropperXView = inflater.inflate(R.layout.cropper_x_view, this, true)
        cropperImageView = cropperXView.findViewById(R.id.CropperImageView)
        cropperControlView = cropperXView.findViewById(R.id.CropperControlView)
        progressBar = cropperXView.findViewById(R.id.ProgressBar)
    }


}