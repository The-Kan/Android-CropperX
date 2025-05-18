package com.devyd.androidcropper.util

import android.util.Log

object LogUtil {

    fun i(fromFile: String,fromFunName: String, msg: String){
        val tag = "$fromFile#$fromFunName"
        Log.i(tag, msg)
    }

    fun e(fromFile: String,fromFunName: String, msg: String){
        val tag = "$fromFile#$fromFunName"
        Log.e(tag, msg)
    }


}