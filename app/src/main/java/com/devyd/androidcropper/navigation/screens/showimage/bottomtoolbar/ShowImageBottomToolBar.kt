package com.devyd.androidcropper.navigation.screens.showimage.bottomtoolbar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.devyd.androidcropper.util.ImmutableList

val BOTTOM_TOOLBAR_HEIGHT_SMALL = 60.dp

@Composable
fun ShowImageBottomToolBar(
    modifier: Modifier,
    bottomToolbarItemList: ImmutableList<BottomToolbarItem>,
    bottomToolbarHeight: Dp = BOTTOM_TOOLBAR_HEIGHT_SMALL,
    bottomToolbarEvent: (BottomToolbarEvent) -> Unit
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(bottomToolbarHeight)
            .background(MaterialTheme.colorScheme.background),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        bottomToolbarItemList.items.forEach {
            BottomToolbarItemLayout(
                bottomToolbarItem = it,
                bottomToolbarEvent = bottomToolbarEvent
            )
        }
    }
}

@Composable
fun BottomToolbarItemLayout(
    modifier: Modifier = Modifier,
    bottomToolbarItem: BottomToolbarItem,
    bottomToolbarEvent: (BottomToolbarEvent) -> Unit
) {

    Column(
        modifier = modifier.clickable {
            bottomToolbarEvent(
                BottomToolbarEvent.OnItemClicked(
                    bottomToolbarItem
                )
            )
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            modifier = Modifier.size(28.dp),
            contentDescription = null,
            imageVector = Icons.Outlined.Crop,
            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground)
        )
    }
}