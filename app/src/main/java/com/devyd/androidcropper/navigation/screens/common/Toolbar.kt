package com.devyd.androidcropper.navigation.screens.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayoutScope
import androidx.constraintlayout.compose.Dimension


@Composable
fun ConstraintLayoutScope.TopToolbarModifier(
    constrainRef: ConstrainedLayoutReference
) = Modifier.constrainAs(constrainRef) {
    top.linkTo(parent.top)
    width = Dimension.matchParent
    height = Dimension.wrapContent
}

@Composable
fun ConstraintLayoutScope.BottomToolbarModifier(
    constrainRef: ConstrainedLayoutReference
) = Modifier.constrainAs(constrainRef) {
    bottom.linkTo(parent.bottom)
    width = Dimension.matchParent
    height = Dimension.wrapContent
}

@Composable
fun TopToolbarHeightPaddingInEdgeToEdge(content : @Composable () -> Unit){

    val topDP = with(LocalDensity.current) {
        WindowInsets.statusBars.getTop(this).toDp()
    }

    Column(modifier = Modifier.padding(top = topDP)) {
        content()
    }
}

@Composable
fun BottomToolbarHeightPaddingInEdgeToEdge(content : @Composable () -> Unit){

    val bottomDp = with(LocalDensity.current) {
        WindowInsets.navigationBars.getBottom(this).toDp()
    }

    Column(modifier = Modifier.padding(bottom = bottomDp)) {
        content()
    }
}