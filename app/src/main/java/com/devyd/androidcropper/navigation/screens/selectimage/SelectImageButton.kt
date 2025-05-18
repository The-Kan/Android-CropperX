package com.devyd.androidcropper.navigation.screens.selectimage


import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.devyd.androidcropper.R
import com.devyd.androidcropper.util.LogUtil


@Composable
fun SelectImageButton(
    modifier: Modifier,
    onImageSelected: (Uri?) -> Unit,
    showProgress: Boolean = false
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(), onResult = onImageSelected
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Button(onClick = {
            imagePickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }) { Text(text = stringResource(R.string.pick_image)) }


        Spacer(modifier = Modifier.height(10.dp))

        Progress(
            modifier = Modifier
                .size(40.dp)
                .alpha(if (showProgress) 1f else 0f),
            progressBarSize = 30.dp,
            progressBarStrokeWidth = 3.dp
        )

    }
}


@Composable
fun Progress(
    modifier: Modifier,
    progressBarSize: Dp,
    progressBarColor: Color = MaterialTheme.colorScheme.onBackground,
    progressBarStrokeWidth: Dp
) {

    Box(modifier = modifier) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(progressBarSize)
                .align(Alignment.Center),
            color = progressBarColor,
            strokeWidth = progressBarStrokeWidth
        )
    }
}