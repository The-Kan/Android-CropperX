package com.devyd.androidcropper.util

import android.content.Context

object AppUtil {

    fun getScreenWidthAndHeight(context: Context): Pair<Int, Int> {
        val disPlayMetrics = context.resources.displayMetrics
        return Pair(disPlayMetrics.widthPixels, disPlayMetrics.heightPixels)
    }
}