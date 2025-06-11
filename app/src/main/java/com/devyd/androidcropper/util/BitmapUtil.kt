package com.devyd.androidcropper.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.devyd.androidcropper.bitmap.BitmapStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

object BitmapUtil {

    private fun getWidthAndHeightFromUri(context: Context, uri: Uri): Pair<Int, Int> {
        val onlyBoundsOption = BitmapFactory.Options()
        onlyBoundsOption.inJustDecodeBounds = true
        onlyBoundsOption.inPreferredConfig = Bitmap.Config.ARGB_8888 // 필요한가? 경계만 읽어오는데
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, onlyBoundsOption)
        }
        return Pair(onlyBoundsOption.outWidth, onlyBoundsOption.outHeight)
    }

    private fun calculateInSampleSize(screenSize: Pair<Int, Int>, bitmapSize: Pair<Int, Int>): Int {
        val (reqWidth, reqHeight) = screenSize
        val (bitmapWidth, bitmapHeight) = bitmapSize

        var inSampleSize = 0

        do {
            inSampleSize++
            val afterWidth = bitmapWidth / inSampleSize
            val afterHeight = bitmapHeight / inSampleSize

        } while (afterWidth > reqWidth && afterHeight > reqHeight)

        return inSampleSize

    }

    private fun resizeBitmapFromRes(
        context: Context,
        uri: Uri,
        onSaveInSampleSize: ((Int) -> Unit)? = null
    ): Bitmap? {
        BitmapFactory.Options().run {
            // 아래 중복인것같은데 getWidthAndHeightFromUri랑
//            inJustDecodeBounds = true
//            context.contentResolver.openInputStream(uri)?.use { inputStream ->
//                BitmapFactory.decodeStream(inputStream, null, this)
//            }

            val screenSize = AppUtil.getScreenWidthAndHeight(context)
            val bitmapSize = getWidthAndHeightFromUri(context, uri)


            inJustDecodeBounds = false

            //inSampleSize = calculateInSampleSize(screenSize, bitmapSize)
            // 잠시 원본 유지
            inSampleSize = 1

            if (onSaveInSampleSize != null) {
                onSaveInSampleSize(inSampleSize)
            }

            Log.i("Deok", "inSampleSize = ${inSampleSize}")

//            val list = ArrayList<Bitmap>()
//
//
//            CoroutineScope(Dispatchers.IO).launch{
//                var cnt = 0
//                repeat(100){
//                    cnt++
//                    delay(1000)
//                    Log.i("Deok","oom test 1 cnt = ${cnt}")
//                    val bitmap: Bitmap? = context.contentResolver.openInputStream(uri)?.use {
//                        BitmapFactory.decodeStream(it, null, BitmapFactory.Options())
//                    }
//                    bitmap?.let { list.add(it) }
//                    LogUtil.logMemoryStats()
//                    Log.i("Deok","oom test 1 100개 끝")
//                }
//            }





            val bitmap: Bitmap? = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, this)
            }

            Log.i("Deok", "oom test 1 생성된 비트맵 높이 너비 result = ${bitmap?.height} ${bitmap?.width}")
            Log.i("Deok", "oom test 1 생성된 비트맵 용량 크기 result = ${bitmap?.allocationByteCount}")
            Log.i("Deok", "oom test 1 생성된 비트맵 용량 크기 result(KByte) = ${bitmap!!.allocationByteCount/1024}")
            Log.i("Deok", "oom test 1 생성된 비트맵 용량 크기 result(MByte) = ${bitmap!!.allocationByteCount/(1024*1024)}")

            return bitmap
        }
    }

    fun getResizedBitmap(
        context: Context,
        uri: Uri,
        onSaveInSampleSize: ((Int) -> Unit)? = null
    ) = flow {
        emit(BitmapStatus.Decoding)
        delay(500) // just Test progress, It will be removed
        val decodedBitmap = resizeBitmapFromRes(context, uri, onSaveInSampleSize)
        if (decodedBitmap != null) {
            emit(BitmapStatus.Success(decodedBitmap))
        } else {
            if (onSaveInSampleSize != null) {
                onSaveInSampleSize(0)
            }
            emit(BitmapStatus.Fail())
        }
    }

    fun saveBitmapToFile(bitmap: Bitmap, file: File) {
        FileOutputStream(file).use {
            // 이 포맷이 CrroperXView에 전달되면 좋겠다.
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
    }
}