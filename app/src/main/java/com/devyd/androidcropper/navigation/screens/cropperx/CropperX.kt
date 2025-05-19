package com.devyd.androidcropper.navigation.screens.cropperx

import android.graphics.Bitmap
import android.util.Log
import android.view.WindowManager
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.devyd.androidcropper.navigation.screens.showimage.bottomtoolbar.BOTTOM_TOOLBAR_HEIGHT_SMALL
import com.devyd.androidcropper.navigation.screens.showimage.toptoolbar.TOP_TOOLBAR_HEIGHT_SMALL
import com.devyd.androidcropper.util.ImmutableBitmap
import com.devyd.androidcropper.util.getActivity

@Composable
fun CropperX(
    immutableBitmap : ImmutableBitmap,
    onDoneClicked : (Bitmap) -> Unit,
    navigateBackPress : () -> Unit
) {

    val context = LocalContext.current
    val activity = LocalContext.current.getActivity()
    val lifecycleOwner = LocalLifecycleOwner.current


    val topToolbarHeight = TOP_TOOLBAR_HEIGHT_SMALL
    val bottomToolbarHeight = BOTTOM_TOOLBAR_HEIGHT_SMALL


    var toolbarVisible by remember { mutableStateOf(false) }

    LaunchedEffect (Unit) {
        toolbarVisible = true
    }

    val previousSoftInputMode = remember {
        activity?.window?.attributes?.softInputMode ?: WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED
    }

    DisposableEffect(Unit) {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        onDispose {
            activity?.window?.setSoftInputMode(previousSoftInputMode)
        }
    }


    var neededCrop by remember { mutableStateOf(false) }


}