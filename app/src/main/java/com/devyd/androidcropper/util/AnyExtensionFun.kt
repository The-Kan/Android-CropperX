package com.devyd.androidcropper.util

import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.activity.ComponentActivity


fun Context.toast(msg: String){
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Context.toast(resId: Int){
    Toast.makeText(this, resources.getString(resId), Toast.LENGTH_SHORT).show()
}

fun Context.getActivity() : ComponentActivity? = when(this){
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}