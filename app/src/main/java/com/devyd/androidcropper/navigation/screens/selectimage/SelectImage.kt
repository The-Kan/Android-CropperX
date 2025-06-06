package com.devyd.androidcropper.navigation.screens.selectimage

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.devyd.androidcropper.bitmap.BitmapStatus
import com.devyd.androidcropper.util.BitmapUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

@Composable
fun SelectImage(
    modifier: Modifier = Modifier,
    onImageLoaded: (Bitmap) -> Unit,
    onSaveInSampleSize : (Int) -> Unit,
    onSaveOriginalImageUri : (Uri) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var resizedBitmapStatus by remember { mutableStateOf<BitmapStatus>(BitmapStatus.None) }

    val onImageSelected = remember<(Uri?) -> Unit> {
        {
            imageUri = it
            imageUri?.let { imageUri -> onSaveOriginalImageUri(imageUri) }
        }
    }


    LaunchedEffect(key1 = imageUri) {
        imageUri?.let {
            BitmapUtil.getResizedBitmap(context, it, onSaveInSampleSize)
                .flowOn(Dispatchers.IO) // 업 스트림 IO에서 진행.
                .collect { bitmapStatus ->
                    resizedBitmapStatus = bitmapStatus
                }
        }
    }


    SelectImageLayout(
        modifier = modifier,
        resizedBitmapStatus = resizedBitmapStatus,
        onImageSelected = onImageSelected,
        onImageLoaded = onImageLoaded
    )
}