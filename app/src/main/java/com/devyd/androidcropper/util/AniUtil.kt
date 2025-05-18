package com.devyd.androidcropper.util

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut

object AniUtil {
    const val TOOLBAR_COLLAPSE_ANIM_DURATION = 250

    val DECELERATE_EASING = CubicBezierEasing(0.1f, 0.8f, 0.3f, 1.0f)
    val ACCELERATE_EASING = CubicBezierEasing(0.1f, 0.05f, 0.1f, 1.0f)


    fun toolbarExpandAnim() = expandIn(
        animationSpec = tween(TOOLBAR_COLLAPSE_ANIM_DURATION, easing = DECELERATE_EASING)
    )

    fun toolbarCollapseAnim() = shrinkOut(
        animationSpec = tween(TOOLBAR_COLLAPSE_ANIM_DURATION, easing = ACCELERATE_EASING)
    )

}