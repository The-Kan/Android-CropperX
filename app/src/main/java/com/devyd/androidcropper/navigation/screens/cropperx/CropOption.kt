package com.devyd.androidcropper.navigation.screens.cropperx

import java.util.UUID

data class CropOption(
    val id: String = UUID.randomUUID().toString(),
    val aspectRatioX: Float,
    val aspectRatioY: Float,
    val label: String = when (aspectRatioX) {
        -1f -> "free"
        1f -> "square"
        else -> "${aspectRatioX.toInt()}:${aspectRatioY.toInt()}"
    }
) {
}