package com.devyd.cropperx.util

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import com.devyd.cropperx.view.CropperXView
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

internal object BitmapUtils {

    val EMPTY_RECT = Rect()
    val EMPTY_RECT_F = RectF()

    fun getRectLeft(points: FloatArray): Float = min(min(min(points[0], points[2]), points[4]), points[6])

    fun getRectTop(points: FloatArray): Float = min(min(min(points[1], points[3]), points[5]), points[7])

    fun getRectRight(points: FloatArray): Float = max(max(max(points[0], points[2]), points[4]), points[6])

    fun getRectBottom(points: FloatArray): Float = max(max(max(points[1], points[3]), points[5]), points[7])

    fun getRectWidth(points: FloatArray): Float = getRectRight(points) - getRectLeft(points)

    fun getRectHeight(points: FloatArray): Float = getRectBottom(points) - getRectTop(points)

    fun getRectCenterX(points: FloatArray): Float = (getRectRight(points) + getRectLeft(points)) / 2f

    fun getRectCenterY(points: FloatArray): Float = (getRectBottom(points) + getRectTop(points)) / 2f

    internal class BitmapSampled(
        val bitmap: Bitmap?,
        val sampleSize: Int,
    )

    fun cropBitmap(
        context: Context,
        loadedImageUri: Uri?,
        cropPoints: FloatArray,
        orgWidth: Int,
        orgHeight: Int,
        fixAspectRatio: Boolean,
        aspectRatioX: Int,
        aspectRatioY: Int,
        reqWidth: Int,
        reqHeight: Int,
    ): BitmapSampled {
        var sampleMulti = 1

        while (true) {
            try {

                return cropBitmap(
                    context = context,
                    loadedImageUri = loadedImageUri!!,
                    cropPoints = cropPoints,
                    orgWidth = orgWidth,
                    orgHeight = orgHeight,
                    fixAspectRatio = fixAspectRatio,
                    aspectRatioX = aspectRatioX,
                    aspectRatioY = aspectRatioY,
                    reqWidth = reqWidth,
                    reqHeight = reqHeight,
                    sampleMulti = sampleMulti,
                )
            } catch (e: OutOfMemoryError) {

                sampleMulti *= 2 // OOM이 발생하면 해상도를 2배씩 줄여나간다. 다운샘플링. 왜 2씩 곱했는가: 빠르고 효율적인 대응
                if (sampleMulti > 16) {
                    throw RuntimeException(
                        "Failed to handle OOM by sampling ($sampleMulti): $loadedImageUri\r\n${e.message}",
                        e,
                    )
                }
            }
        }
    }

    private fun cropBitmap(
        context: Context,
        loadedImageUri: Uri,
        cropPoints: FloatArray,
        fixAspectRatio: Boolean,
        aspectRatioX: Int,
        aspectRatioY: Int,
        sampleMulti: Int,
        rect: Rect,
        width: Int,
        height: Int,
    ): BitmapSampled {
        var result: Bitmap? = null
        val sampleSize: Int
        try {
            val options = BitmapFactory.Options()
            sampleSize = (
                    sampleMulti *
                            calculateInSampleSizeByRequestedSize(
                                width = rect.width(),
                                height = rect.height(),
                                reqWidth = width,
                                reqHeight = height,
                            )
                    )
            options.inSampleSize = sampleSize
            val fullBitmap = decodeImage(
                resolver = context.contentResolver,
                uri = loadedImageUri,
                options = options,
            )
            if (fullBitmap != null) {
                try {

                    val points2 = FloatArray(cropPoints.size)
                    System.arraycopy(cropPoints, 0, points2, 0, cropPoints.size)
                    for (i in points2.indices) {
                        points2[i] = points2[i] / options.inSampleSize
                    }

                    result = cropBitmapObjectWithScale(
                        bitmap = fullBitmap,
                        cropPoints = points2,
                        fixAspectRatio = fixAspectRatio,
                        aspectRatioX = aspectRatioX,
                        aspectRatioY = aspectRatioY, scale = 1f,
                    )
                } finally {
                    if (result != fullBitmap) {
                        fullBitmap.recycle()
                    }
                }
            }
        } catch (e: OutOfMemoryError) {
            result?.recycle()
            throw e
        } catch (e: Exception) {
            throw Exception("cropBitmap Exception")
        }
        return BitmapSampled(result, sampleSize)
    }

    @Throws(FileNotFoundException::class)
    private fun decodeImage(
        resolver: ContentResolver,
        uri: Uri,
        options: BitmapFactory.Options,
    ): Bitmap? {
        do {
            resolver.openInputStream(uri).use {
                try {
                    return BitmapFactory.decodeStream(it, EMPTY_RECT, options)
                } catch (e: OutOfMemoryError) {
                    options.inSampleSize *= 2
                }
            }
        } while (options.inSampleSize <= 512)
        throw Exception("decodeImage Exception")
    }

    private fun cropBitmapObjectWithScale(
        bitmap: Bitmap,
        cropPoints: FloatArray,
        fixAspectRatio: Boolean,
        aspectRatioX: Int,
        aspectRatioY: Int,
        scale: Float,
    ): Bitmap {

        val rect = getRectFromPoints(
            cropPoints,
            bitmap.width,
            bitmap.height,
            fixAspectRatio,
            aspectRatioX,
            aspectRatioY,
        )

        val matrix = Matrix()
        var result = Bitmap.createBitmap(
            bitmap,
            rect.left,
            rect.top,
            rect.width(),
            rect.height(),
            matrix,
            true,
        )
        if (result == bitmap) {

            result = bitmap.copy(bitmap.config!!, false)
        }
        return result
    }


    private fun calculateInSampleSizeByRequestedSize(
        width: Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int,
    ): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            while (height / 2 / inSampleSize > reqHeight && width / 2 / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun cropBitmap(
        context: Context,
        loadedImageUri: Uri,
        cropPoints: FloatArray,
        orgWidth: Int,
        orgHeight: Int,
        fixAspectRatio: Boolean,
        aspectRatioX: Int,
        aspectRatioY: Int,
        reqWidth: Int,
        reqHeight: Int,
        sampleMulti: Int,
    ): BitmapSampled {

        val rect = getRectFromPoints(
            cropPoints,
            orgWidth,
            orgHeight,
            fixAspectRatio,
            aspectRatioX,
            aspectRatioY,
        )
        val width = if (reqWidth > 0) reqWidth else rect.width()
        val height = if (reqHeight > 0) reqHeight else rect.height()
        var result: Bitmap? = null
        var sampleSize = 1
        try {

            val bitmapSampled =
                decodeSampledBitmapRegion(context, loadedImageUri, rect, width, height, sampleMulti)
            result = bitmapSampled.bitmap
            sampleSize = bitmapSampled.sampleSize
        } catch (ignored: Exception) {
        }
        return if (result != null) {
            try {


            } catch (e: OutOfMemoryError) {
                result.recycle()
                throw e
            }
            BitmapSampled(result, sampleSize)
        } else {
            // 부분 디코딩이 실패한 경우, 이미지 전체를 디코딩하여, 원하는 부분만 잘라낸다.
            cropBitmap(
                context,
                loadedImageUri,
                cropPoints,
                fixAspectRatio,
                aspectRatioX,
                aspectRatioY,
                sampleMulti,
                rect,
                width,
                height,
            )
        }
    }

    fun getRectFromPoints(
        cropPoints: FloatArray,
        imageWidth: Int,
        imageHeight: Int,
        fixAspectRatio: Boolean,
        aspectRatioX: Int,
        aspectRatioY: Int,
    ): Rect {
        // 원본 이미지의 크기(imageWidth, imageHeight)를 절대로 초과하지 않도록 제한
        val left = max(0f, getRectLeft(cropPoints)).roundToInt()
        val top = max(0f, getRectTop(cropPoints)).roundToInt()
        val right = min(imageWidth.toFloat(), getRectRight(cropPoints)).roundToInt()
        val bottom = min(imageHeight.toFloat(), getRectBottom(cropPoints)).roundToInt()


        Log.i("Deok", "계산되니? left = ${left}")
        Log.i("Deok", "계산되니? top = ${top}")
        Log.i("Deok", "계산되니? right = ${right}")
        Log.i("Deok", "계산되니? bottom = ${bottom}")

        // Android Rect 객체를 획득
        val rect = Rect(left, top, right, bottom)

        if (fixAspectRatio) {
            fixRectForAspectRatio(rect, aspectRatioX, aspectRatioY)
        }
        return rect
    }

    private fun fixRectForAspectRatio(rect: Rect, aspectRatioX: Int, aspectRatioY: Int) {
        // 크롭 비율 x,y가 같은데 크롭 rect의 실제 width, height가 다른경우 서로 길이를 맞춰 정사각형으로 만든다.
        if (aspectRatioX == aspectRatioY && rect.width() != rect.height()) {
            if (rect.height() > rect.width()) {
                rect.bottom -= rect.height() - rect.width()
            } else {
                rect.right -= rect.width() - rect.height()
            }
        }
    }


    private fun decodeSampledBitmapRegion(
        context: Context,
        uri: Uri,
        rect: Rect,
        reqWidth: Int,
        reqHeight: Int,
        sampleMulti: Int,
    ): BitmapSampled {
        try {
            val options = BitmapFactory.Options()
            options.inSampleSize = sampleMulti * calculateInSampleSizeByRequestedSize(
                width = rect.width(),
                height = rect.height(),
                reqWidth = reqWidth,
                reqHeight = reqHeight,
            )

            context.contentResolver.openInputStream(uri).use {
                val decoder = when {
                    // BitmapRegionDecoder 이미지 일부 영역만을 디코딩할 수 있도록 함.
                    SDK_INT >= 31 -> BitmapRegionDecoder.newInstance(it!!)
                    else -> @Suppress("DEPRECATION") BitmapRegionDecoder.newInstance(it!!, false)
                }

                try {
                    do {
                        try {
                            // InputStream으로부터 파일을 읽어서 rect에 맞춰 crop영역만 디코딩해서 Bitmap을 반환
                            return BitmapSampled(
                                decoder!!.decodeRegion(rect, options),
                                options.inSampleSize,
                            )
                        } catch (e: OutOfMemoryError) {
                            options.inSampleSize *= 2
                        }
                    } while (options.inSampleSize <= 512)
                } finally {
                    decoder?.recycle()
                }
            }
        } catch (e: Exception) {
            throw Exception("decodeSampledBitmapRegion Exception")
        }
        return BitmapSampled(null, 1)
    }

    fun cropBitmapObjectHandleOOM(
        bitmap: Bitmap?,
        cropPoints: FloatArray,
        fixAspectRatio: Boolean,
        aspectRatioX: Int,
        aspectRatioY: Int,
    ): BitmapSampled {
        var scale = 1
        while (true) {
            try {
                val cropBitmap = cropBitmapObjectWithScale(
                    bitmap = bitmap!!,
                    cropPoints = cropPoints,
                    fixAspectRatio = fixAspectRatio,
                    aspectRatioX = aspectRatioX,
                    aspectRatioY = aspectRatioY,
                    scale = 1 / scale.toFloat(),
                )
                return BitmapSampled(cropBitmap, scale)
            } catch (e: OutOfMemoryError) {
                scale *= 2
                if (scale > 8) {
                    throw e
                }
            }
        }
    }

    fun resizeBitmap(
        bitmap: Bitmap?,
        reqWidth: Int,
        reqHeight: Int,
        options: CropperXView.RequestSizeOptions,
    ): Bitmap {
        try {
            if (reqWidth > 0 && reqHeight > 0 && (options === CropperXView.RequestSizeOptions.RESIZE_FIT || options === CropperXView.RequestSizeOptions.RESIZE_INSIDE || options === CropperXView.RequestSizeOptions.RESIZE_EXACT)) {
                var resized: Bitmap? = null
                if (options === CropperXView.RequestSizeOptions.RESIZE_EXACT) {
                    resized = Bitmap.createScaledBitmap(bitmap!!, reqWidth, reqHeight, false)
                } else {
                    val width = bitmap!!.width
                    val height = bitmap.height
                    val scale = max(width / reqWidth.toFloat(), height / reqHeight.toFloat())
                    if (scale > 1 || options === CropperXView.RequestSizeOptions.RESIZE_FIT) {
                        resized = Bitmap.createScaledBitmap(
                            bitmap,
                            (width / scale).toInt(),
                            (height / scale).toInt(),
                            false,
                        )
                    }
                }
                if (resized != null) {
                    if (resized != bitmap) {
                        bitmap.recycle()
                    }
                    return resized
                }
            }
        } catch (e: Exception) {
            Log.w("AIC", "Failed to resize cropped image, return bitmap before resize", e)
        }
        return bitmap!!
    }
}