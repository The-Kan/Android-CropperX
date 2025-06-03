package com.devyd.androidcropper.util

import com.devyd.androidcropper.navigation.screens.cropperx.CropOption

object CropUtil {

    fun getCropVerticalRatioList() : ArrayList<CropOption> {

        val cropOptionList = arrayListOf(
            CropOption(
                aspectRatioX = -1f,
                aspectRatioY = -1f,
            ),
            CropOption(
                aspectRatioX = 1f,
                aspectRatioY = 1f,
            ),
            CropOption(
                aspectRatioX = 9f,
                aspectRatioY = 16f,
            ),
            CropOption(
                aspectRatioX = 4f,
                aspectRatioY = 5f,
            ),
            CropOption(
                aspectRatioX = 5f,
                aspectRatioY = 7f,
            ),
            CropOption(
                aspectRatioX = 3f,
                aspectRatioY = 4f,
            ),
            CropOption(
                aspectRatioX = 3f,
                aspectRatioY = 5f,
            ),
            CropOption(
                aspectRatioX = 2f,
                aspectRatioY = 3f,
            )
        )

        return cropOptionList
    }

    fun getCropHorizontalRatioList() : ArrayList<CropOption> {

        val cropOptionList = arrayListOf(
            CropOption(
                aspectRatioX = -1f,
                aspectRatioY = -1f,
            ),
            CropOption(
                aspectRatioX = 1f,
                aspectRatioY = 1f,
            ),
            CropOption(
                aspectRatioX = 16f,
                aspectRatioY = 9f,
            ),
            CropOption(
                aspectRatioX = 5f,
                aspectRatioY = 4f,
            ),
            CropOption(
                aspectRatioX = 7f,
                aspectRatioY = 5f,
            ),
            CropOption(
                aspectRatioX = 4f,
                aspectRatioY = 3f,
            ),
            CropOption(
                aspectRatioX = 5f,
                aspectRatioY = 3f,
            ),
            CropOption(
                aspectRatioX = 3f,
                aspectRatioY = 2f,
            )
        )

        return cropOptionList
    }
}
