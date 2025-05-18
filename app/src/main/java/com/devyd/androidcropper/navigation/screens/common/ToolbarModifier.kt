package com.devyd.androidcropper.navigation.screens.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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