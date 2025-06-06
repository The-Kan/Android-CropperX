package com.devyd.cropperx.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Matrix
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.util.component1
import androidx.core.util.component2
import com.devyd.cropperx.R
import com.devyd.cropperx.ani.CropperXAnimation
import com.devyd.cropperx.crop.BitmapCroppingWorkerJob
import com.devyd.cropperx.crop.CropOptions
import com.devyd.cropperx.util.BitmapUtils
import java.lang.ref.WeakReference
import kotlin.math.max
import kotlin.math.min

class CropperXView(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs), CropWindowChangeListener {

    private val cropperImageView: ImageView
    private val cropperControlView: CropperControlView?
    private val progressBar: ProgressBar
    private var originalBitmap: Bitmap? = null
    private var imageResource = 0

    private var mLayoutWidth = 0
    private var mLayoutHeight = 0

    private val mImagePoints = FloatArray(8) // 이미지 변환 시 계산을 위한 8개의 좌표값을 저장하는 배열입니다 (예: 4개의 x,y 좌표쌍).
    private val mScaleImagePoints = FloatArray(8) // 스케일링된 이미지 위치 계산을 위한 좌표 배열입니다.

    var imageUri: Uri? = null
        private set

    private var loadedSampleSize = 1

    private var isCropperControlViewVisible = true

    private val mImageMatrix = Matrix()
    private val mImageInverseMatrix = Matrix()


    private var mScaleType: ScaleType // CENTER_CROP, FIT_CENTER 같은 이미지 스케일 방식입니다.


    private var mAutoZoomEnabled = true
    private var mZoom = 1f
    private var mMaxZoom: Int

    private var mAnimation: CropperXAnimation? = null

    private var mZoomOffsetX = 0f
    private var mZoomOffsetY = 0f

    private var mOnCropCompleteListener: OnCropCompleteListener? = null

    private var bitmapCroppingWorkerJob: WeakReference<BitmapCroppingWorkerJob>? = null

    val cropPoints: FloatArray
        get() {
            // Get crop window position relative to the displayed image.
            val cropWindowRect = cropperControlView!!.cropWindowRect
            val points = floatArrayOf(
                cropWindowRect.left,
                cropWindowRect.top,
                cropWindowRect.right,
                cropWindowRect.top,
                cropWindowRect.right,
                cropWindowRect.bottom,
                cropWindowRect.left,
                cropWindowRect.bottom,
            )
            mImageMatrix.invert(mImageInverseMatrix)
            mImageInverseMatrix.mapPoints(points)
            val resultPoints = FloatArray(points.size)
            for (i in points.indices) {
                resultPoints[i] = points[i] * loadedSampleSize
                Log.i("Deok", "계산되니? ${points[i]} , ${loadedSampleSize} -> ${ resultPoints[i]}")
            }
            return resultPoints
        }


    internal companion object {
        /**
         * Determines the specs for the onMeasure function. Calculates the width or height depending on
         * the mode.
         *
         * [measureSpecMode] The mode of the measured width or height.
         * [measureSpecSize] The size of the measured width or height.
         * [desiredSize] The desired size of the measured width or height.
         * @return The final size of the width or height.
         */
        internal fun getOnMeasureSpec(
            measureSpecMode: Int,
            measureSpecSize: Int,
            desiredSize: Int,
        ): Int {
            // Measure Width
            return when (measureSpecMode) {
                // MeasureSpec.EXACTLY 자식 뷰(CropImageView)는 부모 뷰가 요구한 "정확한 크기"(match_parent, 100dp) 를 무조건 따라야 합니다.
                MeasureSpec.EXACTLY -> measureSpecSize // Must be this size
                // MeasureSpec.AT_MOST 부모 뷰가 자식 뷰에게 크기 제한을 제공하지만, 자식 뷰는 그 크기 이하로 조정될 수 있음을 의미합니다. 예를 들어, wrap_content
                MeasureSpec.AT_MOST -> min(
                    desiredSize,
                    measureSpecSize,
                ) // Can't be bigger than...; match_parent value
                // UNSPECIFIED는 제한 없음
                else -> desiredSize // Be whatever you want; wrap_content
            }
        }
    }



    init {
        val option = CropOptions()

        mScaleType = option.scaleType
        mAutoZoomEnabled = option.autoZoomEnabled
        mMaxZoom = option.maxZoom


        val inflater = LayoutInflater.from(context)
        val cropperXView = inflater.inflate(R.layout.cropper_x_view, this, true)
        cropperImageView = cropperXView.findViewById(R.id.CropperImageView)
        cropperImageView.scaleType = ImageView.ScaleType.MATRIX // Matrix 확대 적용을 위해 사용.
        cropperControlView = cropperXView.findViewById(R.id.CropperControlView)
        cropperControlView.setCropWindowChangeListener(this)
        cropperControlView.setOption(option)
        progressBar = cropperXView.findViewById(R.id.ProgressBar)
        progressBar.visibility = INVISIBLE
    }

    fun setImageBitmap(bitmap: Bitmap?) {
        // 초기화 단계이므로 크롭뷰도 초기화되어야함.
        setBitmap(bitmap, 0, imageUri, 1)
    }

    fun setImageBitmap(bitmap: Bitmap?, imageUri: Uri?, loadSampleSize: Int) {
        // 초기화 단계이므로 크롭뷰도 초기화되어야함.
        setBitmap(bitmap, 0, imageUri, loadSampleSize)
    }

    private fun setBitmap(
        bitmap: Bitmap?,
        imageResource: Int,
        imageUri: Uri?,
        loadSampleSize: Int
    ) {
        if (originalBitmap == null || originalBitmap != bitmap) {
            clearImage()
            originalBitmap = bitmap
            cropperImageView.setImageBitmap(originalBitmap)
            this.imageResource = imageResource
            this.imageUri = imageUri
            this.loadedSampleSize = loadSampleSize


        }
        // 이미지 확대, 이동시 적용할 메트릭스가 필요.


        // 크롭컨트롤뷰 리셋 필요.
        setCropperControlViewVisible(true)

    }


    fun setImageCropOptions(options: CropOptions) {
        cropperControlView?.setOption(options)
    }


    private fun clearImage() {
        if (originalBitmap != null && imageResource > 0 || imageUri != null) {
            originalBitmap!!.recycle()
        }

        originalBitmap = null
        imageResource = 0
        imageUri = null
        loadedSampleSize = 1
        cropperImageView.setImageBitmap(null)
        setCropperControlViewVisible(false)
    }

    private fun setCropperControlViewVisible(isVisible: Boolean) {
        if (cropperControlView != null) {
            cropperControlView.visibility = if (isVisible) VISIBLE else INVISIBLE
        }
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec) // 측정 모드를 반환합니다. 예를 들어, EXACTLY, AT_MOST, UNSPECIFIED 등의 값이 있을 수 있습니다.
        val widthSize = MeasureSpec.getSize(widthMeasureSpec) // 뷰가 허용된 최대 크기(또는 원하는 크기)를 반환합니다.
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val bitmap = originalBitmap
        if (bitmap != null) { //  비트맵 이미지가 로드된 상태
            // Bypasses a baffling bug when used within a ScrollView, where heightSize is set to 0.
            if (heightSize == 0) heightSize = bitmap.height
            val desiredWidth: Int
            val desiredHeight: Int
            var viewToBitmapWidthRatio = Double.POSITIVE_INFINITY
            var viewToBitmapHeightRatio = Double.POSITIVE_INFINITY
            // Checks if either width or height needs to be fixed
            if (widthSize < bitmap.width) {
                viewToBitmapWidthRatio = widthSize.toDouble() / bitmap.width.toDouble()
            }
            if (heightSize < bitmap.height) {
                viewToBitmapHeightRatio = heightSize.toDouble() / bitmap.height.toDouble()
            }
            // If either needs to be fixed, choose the smallest ratio and calculate from there
            if (viewToBitmapWidthRatio != Double.POSITIVE_INFINITY ||
                viewToBitmapHeightRatio != Double.POSITIVE_INFINITY
            ) {
                // 세로의 비율이 더 커서 여유가 있는 경우, 가로를 가득채우고 세로를 여유롭게 채운다.
                if (viewToBitmapWidthRatio <= viewToBitmapHeightRatio) {
                    desiredWidth = widthSize
                    desiredHeight = (bitmap.height * viewToBitmapWidthRatio).toInt()
                } else { // 가로의 비율이 더 커서 여유가 있는 경우, 세로를 가득채우고 가로를 여유롭게 채운다.
                    desiredHeight = heightSize
                    desiredWidth = (bitmap.width * viewToBitmapHeightRatio).toInt()
                }
            } else {
                // Otherwise, the picture is within frame layout bounds. Desired width is simply picture
                // size
                desiredWidth = bitmap.width
                desiredHeight = bitmap.height
            }


            val width = getOnMeasureSpec(widthMode, widthSize, desiredWidth)
            val height = getOnMeasureSpec(heightMode, heightSize, desiredHeight)
            mLayoutWidth = width
            mLayoutHeight = height

            Log.i("Deok", "widthMode = ${widthMode}, widthSize = ${widthSize}, bitmap.width = ${bitmap.width}, mLayoutWidth = ${mLayoutWidth}")

            setMeasuredDimension(mLayoutWidth, mLayoutHeight)
        } else {
            setMeasuredDimension(widthSize, heightSize)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        Log.i("Deok", "left = ${left}, top = ${top}, right = ${right}, bottom = ${bottom}")

        if (mLayoutWidth > 0 && mLayoutHeight > 0) {
            if (originalBitmap != null) {

                applyImageMatrix((right-left).toFloat(), (bottom-top).toFloat(), false, false)

            } else {
                updateImageBounds(true)
            }
        } else {
            updateImageBounds(true)
        }
    }

    private fun applyImageMatrix(width: Float, height: Float, center: Boolean, animate: Boolean) {
        Log.i("Deok1", "!!!! applyImageMatrix Start!!!!")
        val bitmap = originalBitmap
        if (bitmap != null && width > 0 && height > 0) {
            mImageMatrix.invert(mImageInverseMatrix)

            val cropRect = cropperControlView!!.cropWindowRect
            Log.i("Deok1", "cropRect before")
            Log.i("Deok1", "cropRect.top = ${cropRect.top}")
            Log.i("Deok1", "cropRect.bottom = ${cropRect.bottom}")
            Log.i("Deok1", "cropRect.left = ${cropRect.left}")
            Log.i("Deok1", "cropRect.right = ${cropRect.right}")



            mImageInverseMatrix.mapRect(cropRect)

            mImageMatrix.reset()
            mImageMatrix.postTranslate(
                // (width - bitmap.width) / 2: 이미지가 중앙에 오도록 가로 방향으로 이동
                // (height - bitmap.height) / 2: 이미지가 중앙에 오도록 세로 방향으로 이동
                (width - bitmap.width) / 2,
                (height - bitmap.height) / 2,
            )
            // 중앙으로 오게함.
            Log.i("Deok1", "postTranslate")
            mapImagePointsByImageMatrix()

            val scale = min(
                width / BitmapUtils.getRectWidth(mImagePoints),
                height / BitmapUtils.getRectHeight(mImagePoints),
            )
            if (mScaleType == ScaleType.FIT_CENTER || mScaleType == ScaleType.CENTER_INSIDE && scale < 1 ||
                scale > 1 && mAutoZoomEnabled
            ) {
                // 중앙 좌표를 구하여, 중앙을 기준으로 스케일링을 적용합니다.
                mImageMatrix.postScale(
                    scale,
                    scale,
                    BitmapUtils.getRectCenterX(mImagePoints),
                    BitmapUtils.getRectCenterY(mImagePoints),
                )
                // 중앙에서 확대시킴.
                Log.i("Deok1", "postScale 1")
                mapImagePointsByImageMatrix()
            }
            mImagePointsLog("snapEdgesToBounds 1'''")

            val scaleX = mZoom
            val scaleY = mZoom

            mImageMatrix.postScale(
                scaleX,
                scaleY,
                BitmapUtils.getRectCenterX(mImagePoints),
                BitmapUtils.getRectCenterY(mImagePoints),
            )
            // crop 상황에따라 mZoom 확대시킴
            Log.i("Deok1", "postScale 2 mZoom = ${mZoom}")
            mapImagePointsByImageMatrix()

            // cropRect도 다시 **화면 좌표계** 기준으로 변환해줘야 합니다.
            mImageMatrix.mapRect(cropRect)

            Log.i("Deok1", "cropRect mid")
            Log.i("Deok1", "cropRect.top = ${cropRect.top}")
            Log.i("Deok1", "cropRect.bottom = ${cropRect.bottom}")
            Log.i("Deok1", "cropRect.left = ${cropRect.left}")
            Log.i("Deok1", "cropRect.right = ${cropRect.right}")



            // 중심점이 크롭 Rect의 중심을 기준으로 계산됨.
            if(center){
                mZoomOffsetX =
                    if (width > BitmapUtils.getRectWidth(mImagePoints)) {
                        0f
                    } else {
                        // width / 2 - cropRect.centerX() -> crop 중앙을 뷰의 중앙에 맞추기 위하여 이동해야하는 량
                        // -BitmapUtils.getRectLeft(mImagePoints) -> 이미지 왼쪽 모서리가 뷰의 왼쪽(0)보다 더 왼쪽으로 나가지 않도록 하는 최대 오른쪽 이동량
                        // getWidth() - BitmapUtils.getRectRight(mImagePoints) -> 이미지 오른쪽 모서리가 뷰의 오른쪽(width)보다 더 오른쪽으로 나가지 않도록 하는 최소 왼쪽 이동량

                        max(
                            min(
                                width / 2 - cropRect.centerX(),
                                -BitmapUtils.getRectLeft(mImagePoints),
                            ),
                            getWidth() - BitmapUtils.getRectRight(mImagePoints),
                        ) / scaleX // / scaleX는 "지금 계산한 오프셋이 확대된 이미지에서 어느 정도 픽셀 이동을 의미하는지"로 보정하는 것.
                    }

                mZoomOffsetY =
                    if (height > BitmapUtils.getRectHeight(mImagePoints)) {
                        0f
                    } else {
                        max(
                            min(
                                height / 2 - cropRect.centerY(),
                                -BitmapUtils.getRectTop(mImagePoints),
                            ),
                            getHeight() - BitmapUtils.getRectBottom(mImagePoints),
                        ) / scaleY
                    }
            } else {
                // 사용자가 이미지를 확대하거나 이동했을 때,
                //크롭 창이 이미지 바깥으로 밀려날 수 있음.
                //그래서 현재 mZoomOffsetX / mZoomOffsetY 값을 조정해서, 크롭 창이 항상 화면 내부에 있도록 함.
                Log.i("Deok1", "tt  before")
                Log.i("Deok1", "tt  mZoomOffsetX * scaleX= ${mZoomOffsetX * scaleX}")
                Log.i("Deok1", "tt -cropRect.left = ${-cropRect.left}")
                Log.i("Deok1", "tt -cropRect.right + width = ${-cropRect.right + width}")
                Log.i("Deok1", "tt -cropRect.right = ${-cropRect.right}")
                Log.i("Deok1", "tt width = ${ width}")


                // mZoomOffsetX * scaleX -> crop 중앙을 뷰의 중앙에 맞추기 위하여 이동해야하는 량
                // -cropRect.left -> crop 왼쪽이 왼쪽 바깥으로 벗어나지 않도록 최대선을 정함.
                // -cropRect.right + width -> crop 오른쪽이 오른쪽 바깥으로 벗어나지 않도록 최소선을 정함.
                mZoomOffsetX = (
                        min(
                            max(mZoomOffsetX * scaleX, -cropRect.left),
                            -cropRect.right + width,
                        ) / scaleX
                        )

                Log.i("Deok1", "tt  after")
                Log.i("Deok1", "tt  mZoomOffsetX * scaleX= ${mZoomOffsetX * scaleX}")
                Log.i("Deok1", "tt -cropRect.left = ${-cropRect.left}")
                Log.i("Deok1", "tt -cropRect.right + width = ${-cropRect.right + width}")

                mZoomOffsetY = (
                        min(
                            max(mZoomOffsetY * scaleY, -cropRect.top),
                            -cropRect.bottom + height,
                        ) / scaleY
                        )
            }

            mImageMatrix.postTranslate(mZoomOffsetX * scaleX, mZoomOffsetY * scaleY)
            cropRect.offset(mZoomOffsetX * scaleX, mZoomOffsetY * scaleY)
            cropperControlView.cropWindowRect = cropRect
            mImagePointsLog("snapEdgesToBounds 1''")
            mapImagePointsByImageMatrix()
            cropperControlView.invalidate()


            if(animate){
                mImagePointsLog("snapEdgesToBounds 1'")
                mAnimation!!.setEndState(mImagePoints, mImageMatrix)
                cropperImageView.startAnimation(mAnimation)
                mImagePointsLog("snapEdgesToBounds 2'")
            } else {
                mImagePointsLog("snapEdgesToBounds 1")
                cropperImageView.imageMatrix = mImageMatrix
                mImagePointsLog("snapEdgesToBounds 2")
            }

            updateImageBounds(false)

            Log.i("Deok1", "cropRect after")
            Log.i("Deok1", "cropRect.top = ${cropRect.top}")
            Log.i("Deok1", "cropRect.bottom = ${cropRect.bottom}")
            Log.i("Deok1", "cropRect.left = ${cropRect.left}")
            Log.i("Deok1", "cropRect.right = ${cropRect.right}")
        }
    }

    private fun mImagePointsLog(tag: String){
        Log.i("Deok1", "${tag} mImagePointsLog")
        Log.i("Deok1", "${tag} mImagePointsLog 좌상단 = ${mImagePoints[0]} , ${mImagePoints[1]}")
        Log.i("Deok1", "${tag} mImagePointsLog 우상단 = ${mImagePoints[2]} , ${mImagePoints[3]}")
        Log.i("Deok1", "${tag} mImagePointsLog 좌하단 = ${mImagePoints[6]} , ${mImagePoints[7]}")
        Log.i("Deok1", "${tag} mImagePointsLog 우하단 = ${mImagePoints[4]} , ${mImagePoints[5]}")
    }


    private fun mapImagePointsByImageMatrix() {
        // 좌상단 (0, 0)
        mImagePoints[0] = 0f
        mImagePoints[1] = 0f
        // 우상단 (w, 0)
        mImagePoints[2] = originalBitmap!!.width.toFloat()
        mImagePoints[3] = 0f
        // 우하단 (w, h)
        mImagePoints[4] = originalBitmap!!.width.toFloat()
        mImagePoints[5] = originalBitmap!!.height.toFloat()
        // 좌하단 (0, h)
        mImagePoints[6] = 0f
        mImagePoints[7] = originalBitmap!!.height.toFloat()

        Log.i("Deok", "originalBitmap!!.width = ${originalBitmap!!.width.toFloat()}, originalBitmap!!.height = ${originalBitmap!!.height.toFloat()}")
        // mapPoints()는 해당 매트릭스를 mImagePoints에 적용해서, 변환된 좌표를 직접 mImagePoints 배열에 덮어씌웁니다.
        // x,y 모두 150씩 이동시킨 네 꼭지점을 mImagePoints에 저장하라와 같은 동작.
        mImageMatrix.mapPoints(mImagePoints)
        mImagePointsLog("mImagePointsLog")


        mScaleImagePoints[0] = 0f
        mScaleImagePoints[1] = 0f
        mScaleImagePoints[2] = 100f
        mScaleImagePoints[3] = 0f
        mScaleImagePoints[4] = 100f
        mScaleImagePoints[5] = 100f
        mScaleImagePoints[6] = 0f
        mScaleImagePoints[7] = 100f
        mImageMatrix.mapPoints(mScaleImagePoints)
    }

    private fun updateImageBounds(clear: Boolean) {
        // 크롭 오버레이의 사각형 위치 갱신
        Log.i("Deok", "snapEdgesToBounds updateImageBounds clear = ${clear}")
        if(originalBitmap != null && !clear){

            // 확대 적용이 된 너비 * scaleFactorWidth -> 원본 너비   ex) 1519.2463 * 0.39493266 = 599.9999 (원본 사진 너비)
            // 현재 작은 사진 기준으로 loadedSampleSize가 1인데, 큰 원본 사진이 들어오는 경우, loadedSampleSize가 변할 수 있으므로 수정해야함!!!
            val scaleFactorWidth =
                100f * loadedSampleSize / BitmapUtils.getRectWidth(mScaleImagePoints)
            val scaleFactorHeight =
                100f * loadedSampleSize / BitmapUtils.getRectHeight(mScaleImagePoints)

            //현재 View의 너비, 높이, 그리고 스케일 비율을 넘겨줌
            //이를 통해 크롭 가능한 영역의 최대치(=제한 범위)를 설정
            cropperControlView!!.setCropWindowLimits(
                width.toFloat(),
                height.toFloat(),
                scaleFactorWidth,
                scaleFactorHeight
            )
            Log.i("Deok", "snapEdgesToBounds scaleFactorWidth ${scaleFactorWidth} , width.toFloat() = ${width.toFloat()}")
            Log.i("Deok", "snapEdgesToBounds loadedSampleSize ${loadedSampleSize} , BitmapUtils.getRectWidth(mScaleImagePoints) = ${BitmapUtils.getRectWidth(mScaleImagePoints)}")
            Log.i("Deok", "크기제한 width = ${width}, height = ${height}")

        }

        mImagePointsLog("snapEdgesToBounds")
        cropperControlView!!.setBounds(if (clear) null else mImagePoints, width, height)
    }


    fun startCropImage(
        reqWidth: Int = 0,
        reqHeight: Int = 0,
        options: RequestSizeOptions = RequestSizeOptions.RESIZE_INSIDE,
    ) {
        requireNotNull(mOnCropCompleteListener) { "mOnCropCompleteListener is not set" }
        startCropWorkerTask(
            reqWidth = reqWidth,
            reqHeight = reqHeight,
            options = options,
        )
    }

    fun setInSampleSize(inSampleSize :Int){
        loadedSampleSize = inSampleSize
    }

    private fun startCropWorkerTask(
        reqWidth: Int,
        reqHeight: Int,
        options: RequestSizeOptions,
    ) {
        val bitmap = originalBitmap
        if (bitmap != null) {
            val currentTask =
                if (bitmapCroppingWorkerJob != null) bitmapCroppingWorkerJob!!.get() else null
            currentTask?.cancel()

            Log.i("Deok", "originalBitmap ${originalBitmap?.height} ${originalBitmap?.width}")
            Log.i("Deok", "loadedSampleSize = ${loadedSampleSize}")

            val (orgWidth, orgHeight) =
                if (loadedSampleSize > 1 || options == RequestSizeOptions.SAMPLING) {
                    Pair((bitmap.width * loadedSampleSize), (bitmap.height * loadedSampleSize))
                } else {
                    Pair(0, 0)
                }


            // 작업(launch { … })이 끝나면 내부 코루틴이 더 이상 이 객체를 강하게 잡고 있지 않으므로 메모리 회수될 수 있도록 함.
            bitmapCroppingWorkerJob = WeakReference(
                BitmapCroppingWorkerJob(
                    context = context,
                    cropperXViewReference = WeakReference(this),
                    uri = imageUri,
                    bitmap = bitmap,
                    cropPoints = cropPoints,
                    orgWidth = orgWidth,
                    orgHeight = orgHeight,
                    fixAspectRatio = cropperControlView!!.isFixAspectRatio,
                    aspectRatioX = cropperControlView.aspectRatioX,
                    aspectRatioY = cropperControlView.aspectRatioY,
                    reqWidth = if (options != RequestSizeOptions.NONE) reqWidth else 0,
                    reqHeight = if (options != RequestSizeOptions.NONE) reqHeight else 0,
                    options = options,
                ),
            )

            bitmapCroppingWorkerJob!!.get()!!.start()

           // 프로그래스 보이게 업데이트
            //setProgressBarVisibility()
        }
    }


    //크롭 윈도우가 변경될 때 호출되는 핸들러 함수입니다.
    private fun handleCropWindowChanged(inProgress: Boolean, animate: Boolean) {
        val width = width
        val height = height
        if (originalBitmap != null && width > 0 && height > 0) {
            val cropRect = cropperControlView!!.cropWindowRect // 현재 크롭 윈도우(사용자가 지정한 잘라낼 영역)의 사각형(RectF)을 가져옵니다
            if (inProgress) { // 사용자가 크롭 조작을 하는 중일 경우
                //크롭 영역이 화면 바깥으로 나간 경우
                if (cropRect.left < 0 || cropRect.top < 0 || cropRect.right > width || cropRect.bottom > height) {
                    applyImageMatrix( // 이미지 행렬을 다시 계산해서 위치를 재조정
                        width = width.toFloat(),
                        height = height.toFloat(),
                        center = false,
                        animate = false,
                    )
                }
            } else if (mAutoZoomEnabled || mZoom > 1) { // 사용자가 조작을 끝냈고, 자동 줌이 활성화됐거나 현재 줌 상태가 1보다 클 때만 처리합니다.
                var newZoom = 0f
                // keep the cropping window covered area to 50%-65% of zoomed sub-area
                // 크롭 영역이 화면에 너무 작게 보일 때 자동으로 확대해서 보기 좋게 만들기 위한 로직이야.
                if (mZoom < mMaxZoom && cropRect.width() < width * 0.65f && cropRect.height() < height * 0.65f) {
                    newZoom = min(
                        mMaxZoom.toFloat(),
                        min(
                            (width * mZoom * 0.8f) / cropRect.width(),
                            (height * mZoom * 0.8f) / cropRect.height(),
                        ),
                    )
                }
                // 크롭 윈도우가 너무 크게 잡혀 있을 때 확대 비율을 줄이기 위한 로직
                if (mZoom > 1 && (cropRect.width() > width * 0.81f || cropRect.height() > height * 0.81f)) {
                    newZoom = max(
                        1f,
                        min(
                            (width * mZoom * 0.66f) / cropRect.width(),
                            (height * mZoom * 0.66f) / cropRect.height(),
                            // 1. cropRect.width() / mZoom는 실제로 크롭된 이미지의 크기
                            // 2. 실제로 크롭된 이미지의 크기가 화면의 0.51 비율을 차지하는 배율 계산.
                        ),
                    )
                }
                if (!mAutoZoomEnabled) newZoom = 1f

                if (newZoom > 0 && newZoom != mZoom) {
                    if (animate) {
                        if (mAnimation == null) {
                            // lazy create animation single instance
                            mAnimation = CropperXAnimation(cropperImageView, cropperControlView)
                        }
                        // set the state for animation to start from
                        mAnimation!!.setStartState(mImagePoints, mImageMatrix)
                    }
                    mZoom = newZoom
                    applyImageMatrix(width.toFloat(), height.toFloat(), true, animate)
                }
            }

        }
    }


    override fun onCropWindowChanged(inProgress: Boolean) {
        handleCropWindowChanged(inProgress, true)
    }

    fun setOnCropImageCompleteListener(listener: OnCropCompleteListener?) {
        mOnCropCompleteListener = listener
    }

    fun interface OnCropCompleteListener {

        fun onCropImageComplete(view: CropperXView, result: CropResult)
    }

    open class CropResult internal constructor(
        val bitmap: Bitmap?,
        val error: Exception?,

    ) {
        val isSuccessful: Boolean
            get() = error == null
    }

    enum class RequestSizeOptions {
        NONE,

        SAMPLING,

        RESIZE_INSIDE,

        RESIZE_FIT,

        RESIZE_EXACT,
    }

    internal fun onImageCroppingAsyncComplete(result: BitmapCroppingWorkerJob.Result) {
        bitmapCroppingWorkerJob = null

        //프로그래스 안보이게 업데이트
        //setProgressBarVisibility()
        val listener = mOnCropCompleteListener
        if (listener != null) {
            val cropResult = CropResult(
                bitmap = result.bitmap,
                error = result.error,
            )
            listener.onCropImageComplete(this, cropResult)
        }
    }
}


