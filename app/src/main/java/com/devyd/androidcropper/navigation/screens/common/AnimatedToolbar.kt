package com.devyd.androidcropper.navigation.screens.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.devyd.androidcropper.util.AniUtil

@Composable
fun AnimatedToolbar(
    modifier : Modifier,
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit
){
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = AniUtil.toolbarExpandAnim(),
        exit = AniUtil.toolbarCollapseAnim()
    ) {
        content()
    }
}