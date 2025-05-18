package com.devyd.androidcropper.navigation.screens.showimage

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devyd.androidcropper.state.ShowImageState
import com.devyd.androidcropper.viewmodel.ShowImageViewModel


@Composable
fun ShowImage(
    modifier: Modifier = Modifier,
    initialState: ShowImageState,
    navigateCropperX: (ShowImageState) -> Unit,
    navigateSelectImage: () -> Unit,
    navigateBackPress: () -> Unit
) {

    val viewModel: ShowImageViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        viewModel.initState(initialState)
    }

    val state by viewModel.state.collectAsStateWithLifecycle()


    if (state.undoStack.isNotEmpty()) {

        val bitmap = viewModel.getCurrentBitmap()

        ShowImageLayout(modifier = modifier, bitmap = bitmap)
    }


}

@Composable
fun ShowImageLayout(
    modifier: Modifier,
    bitmap: Bitmap
) {

    Image(
        modifier = Modifier.fillMaxSize(),
        bitmap = bitmap.asImageBitmap(),
        contentDescription = null,
        contentScale = ContentScale.Fit,
    )
}