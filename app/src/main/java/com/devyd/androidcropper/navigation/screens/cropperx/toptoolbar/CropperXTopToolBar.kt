package com.devyd.androidcropper.navigation.screens.cropperx.toptoolbar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.devyd.androidcropper.util.SizeUtil

@Composable
fun CropperXTopToolBar(
    modifier: Modifier,
    toolbarHeight: Dp = SizeUtil.TOOLBAR_HEIGHT_SMALL,
    onCloseClicked: () -> Unit,
    onDoneClicked: () -> Unit
) {

    Row(
        modifier = modifier
            .height(toolbarHeight)
            .background(MaterialTheme.colorScheme.background),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically


    ) {
        Image(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .size(30.dp)
                .clickable {
                    onCloseClicked()
                },
            contentDescription = null,
            imageVector = Icons.Default.Close,
            colorFilter = ColorFilter.tint(
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        Image(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .size(30.dp)
                .clickable {
                    onDoneClicked()
                },
            contentDescription = null,
            imageVector = Icons.Default.Check,
            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground)
        )
    }

}