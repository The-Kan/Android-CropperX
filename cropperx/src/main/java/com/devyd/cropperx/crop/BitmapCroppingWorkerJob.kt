package com.devyd.cropperx.crop

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.devyd.cropperx.util.BitmapUtils
import com.devyd.cropperx.view.CropperXView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext

internal class BitmapCroppingWorkerJob(
    private val context: Context,
    private val cropperXViewReference: WeakReference<CropperXView>,
    private val uri: Uri?,
    private val bitmap: Bitmap?,
    private val cropPoints: FloatArray,
    private val orgWidth: Int,
    private val orgHeight: Int,
    private val fixAspectRatio: Boolean,
    private val aspectRatioX: Int,
    private val aspectRatioY: Int,
    private val reqWidth: Int,
    private val reqHeight: Int,
    private val options: CropperXView.RequestSizeOptions,
) : CoroutineScope {
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job

    fun start() {
        // 비트맵 디코딩은 CPU 바운드 작업에 적합
        job = launch(Dispatchers.Default) {
            try {
                if (isActive) {
                    val bitmapSampled: BitmapUtils.BitmapSampled
                    when {
                        uri != null -> {
                            Log.i("Deok", "uri crop")
                            // uri가 존재하니 이미지 부분 디코딩을 통한 크롭
                            bitmapSampled = BitmapUtils.cropBitmap(
                                context = context,
                                loadedImageUri = uri,
                                cropPoints = cropPoints,
                                orgWidth = orgWidth,
                                orgHeight = orgHeight,
                                fixAspectRatio = fixAspectRatio,
                                aspectRatioX = aspectRatioX,
                                aspectRatioY = aspectRatioY,
                                reqWidth = reqWidth,
                                reqHeight = reqHeight,
                            )
                        }
                        // 원본 bitmap이 있으니 원본에서 크롭.
                        bitmap != null -> {
                            Log.i("Deok", "bitmap crop")
                            bitmapSampled = BitmapUtils.cropBitmapObjectHandleOOM(
                                bitmap = bitmap,
                                cropPoints = cropPoints,
                                fixAspectRatio = fixAspectRatio,
                                aspectRatioX = aspectRatioX,
                                aspectRatioY = aspectRatioY,
                            )
                        }
                        else -> {
                            onPostExecute(
                                Result(
                                    bitmap = null,
                                    uri = null,
                                    error = null,
                                    sampleSize = 1,
                                ),
                            )
                            return@launch
                        }
                    }

                    val resizedBitmap = BitmapUtils.resizeBitmap(
                        bitmap = bitmapSampled.bitmap,
                        reqWidth = reqWidth,
                        reqHeight = reqHeight,
                        options = options,
                    )

                    // 비트맵을 디스크에 저장하는 IO를 위한 코루틴
                    launch(Dispatchers.IO) {

                        onPostExecute(
                            Result(
                                bitmap = resizedBitmap,
                                uri = null,
                                sampleSize = bitmapSampled.sampleSize,
                                error = null,
                            ),
                        )
                    }
                }
            } catch (throwable: Exception) {
                onPostExecute(
                    Result(
                        bitmap = null,
                        uri = null,
                        error = throwable,
                        sampleSize = 1,
                    ),
                )
            }
        }
    }

    private suspend fun onPostExecute(result: Result) {
        // UI 업데이트를 위해 Main 스레드에서 실행하도록 함.
        withContext(Dispatchers.Main) {
            var completeCalled = false
            if (isActive) {
                // 뷰가 파괴되어(onDestroyView() 등) 메모리에서 해제되어야할 시점에 콜백을 위한 참조 떄문에 해제되지 않으면 안됨. 곧바로 해제 되도록 함.
                cropperXViewReference.get()?.let {
                    completeCalled = true
                    // 콜백으로 result 결과를 날려줌.
                    it.onImageCroppingAsyncComplete(result)
                }
            }

            if (!completeCalled && result.bitmap != null) {
                // 비트맵 빠른 해제
                result.bitmap.recycle()
            }
        }
    }

    fun cancel() = job.cancel()

    internal data class Result(
        val bitmap: Bitmap?,
        val uri: Uri?,
        val error: Exception?,
        val sampleSize: Int,
    )
}