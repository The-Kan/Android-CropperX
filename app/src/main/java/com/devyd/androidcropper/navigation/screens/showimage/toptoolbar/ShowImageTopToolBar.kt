package com.devyd.androidcropper.navigation.screens.showimage.toptoolbar

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
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.devyd.androidcropper.util.SizeUtil



@Composable
fun ShowImageTopToolBar(
    modifier: Modifier,
    isUndoPossible: Boolean = false,
    isRedoPossible: Boolean = false,
    isSavePossible: Boolean = false,
    undo: () -> Unit,
    redo: () -> Unit,
    save: () -> Unit,
    close: () -> Unit,
    toolBarHeight: Dp = SizeUtil.TOOLBAR_HEIGHT_SMALL
) {
    Row(
        modifier = modifier
            .height(toolBarHeight)
            .background(MaterialTheme.colorScheme.background),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Image(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .size(30.dp)
                .clickable { close() },
            contentDescription = null,
            imageVector = Icons.Default.Close,
            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground)
        )

        Spacer(modifier = Modifier.weight(1f))


        Image(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .size(30.dp)
                .clickable(enabled = isUndoPossible) { undo() },
            contentDescription = null,
            imageVector = Icons.AutoMirrored.Filled.Undo,
            colorFilter = ColorFilter.tint(
                color = if (isUndoPossible) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    MaterialTheme.colorScheme.background
                }
            )
        )


        Image(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .size(30.dp)
                .clickable(enabled = isRedoPossible) { redo() },
            contentDescription = null,
            imageVector = Icons.AutoMirrored.Filled.Redo,
            colorFilter = ColorFilter.tint(
                color = if (isRedoPossible) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    MaterialTheme.colorScheme.background
                }
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        Image(
            modifier = Modifier
                .padding(10.dp)
                .size(30.dp)
                .clickable(enabled = isSavePossible) { save() },
            contentDescription = null,
            imageVector = Icons.Default.SaveAlt,
            colorFilter = ColorFilter.tint(
                color = if (isSavePossible) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    MaterialTheme.colorScheme.background
                }
            )
        )
    }
}