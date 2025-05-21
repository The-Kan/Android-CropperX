package com.devyd.androidcropper.navigation.screens.selectimage

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devyd.androidcropper.bitmap.BitmapStatus

@Composable
fun SelectImageLayout(
    modifier: Modifier,
    resizedBitmapStatus: BitmapStatus,
    onImageSelected: (Uri?) -> Unit,
    onImageLoaded : (Bitmap) -> Unit
) {

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Android CropperX", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(30.dp))

        when (resizedBitmapStatus) {
            BitmapStatus.None -> SelectImageDefaultLayout(
                modifier = modifier,
                onImageSelected = onImageSelected
            )

            BitmapStatus.Decoding -> SelectImageButton(
                modifier = modifier,
                onImageSelected = onImageSelected,
                showProgress = true
            )
            is BitmapStatus.Fail -> TODO()
            is BitmapStatus.Success -> {
                onImageLoaded(resizedBitmapStatus.bitmap)
                SelectImageButton(
                    modifier = modifier,
                    onImageSelected = onImageSelected
                )

            }
        }
    }
}


@Composable
fun SelectImageDefaultLayout(
    modifier: Modifier,
    onImageSelected: (Uri?) -> Unit
) {
    SelectImageButton(modifier = modifier, onImageSelected = onImageSelected)
}
