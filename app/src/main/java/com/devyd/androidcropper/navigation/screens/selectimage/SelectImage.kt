package com.devyd.androidcropper.navigation.screens.selectimage

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
import com.devyd.androidcropper.bitmap.BitmapStatus
import com.devyd.androidcropper.util.BitmapUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

@Composable
fun SelectImage(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var resizedBitmapStatus by remember { mutableStateOf<BitmapStatus>(BitmapStatus.None) }

    val onImageSelected = remember<(Uri?) -> Unit> {
        {
            imageUri = it
        }
    }

    LaunchedEffect(key1 = imageUri) {
        imageUri?.let {
            withContext(Dispatchers.IO){
                BitmapUtil.getResizedBitmap(context, it).onEach {
                    resizedBitmapStatus = it
                }.collect()
            }
        }
    }

    Text(text="SelectImage")


}