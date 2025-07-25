package com.devyd.cropperx.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.devyd.cropperx.crop.CropOptions
import com.devyd.cropperx.crop.CropRectController
import com.devyd.cropperx.crop.CropRectMoveController
import com.devyd.cropperx.util.BitmapUtils
import java.util.Arrays
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

internal class CropperControlView (context : Context, attrs : AttributeSet?) : View(context, attrs) {

    private var borderPaint : Paint? = null
    private var cropRectController = CropRectController()

    var cropShape : CropShape? = null
        private set

    private var options: CropOptions? = null

    // 이미지가 배치된 좌표 4가지
    // 회전·스케일·패닝이 모두 적용된 이후에 뷰(Canvas) 위에 실제로 그려진 이미지의 네 꼭짓점 좌표
    private val mBoundsPoints = FloatArray(8)

    private var initializedCropWindow = false

    // 초기 크롭 윈도우 패딩
    private var mInitialCropWindowPaddingRatio = 0f

    private val mInitialCropWindowRect = Rect()

    var cropWindowRect: RectF
        get() = cropRectController.getRect()
        set(rect) {
            cropRectController.setRect(rect)
        }

    var isFixAspectRatio = false
        private set

    private var mAspectRatioX = 0
    private var mAspectRatioY = 0

    // 크롭 윈도우가 가져야 할 목표 가로:세로 비율
    private var mTargetAspectRatio = mAspectRatioX.toFloat() / mAspectRatioY

    private val mCalcBounds = RectF()

    private var mTouchRadius = 0f
    private var mCropCornerRadius: Float = 0f
    var cornerShape: CropCornerShape? = null
        private set

    // 가로 방향 비율(aspect ratio X 값
    var aspectRatioX: Int
        get() = mAspectRatioX
        set(aspectRatioX) {
            require(aspectRatioX > 0) { "Cannot set aspect ratio value to a number less than or equal to 0." }
            if (mAspectRatioX != aspectRatioX) {
                mAspectRatioX = aspectRatioX
                mTargetAspectRatio = mAspectRatioX.toFloat() / mAspectRatioY
                if (initializedCropWindow) {
                    initCropWindow()
                    invalidate()
                }
            }
        }

    var aspectRatioY: Int
        get() = mAspectRatioY
        set(aspectRatioY) {
            require(aspectRatioY > 0) { "Cannot set aspect ratio value to a number less than or equal to 0." }
            if (mAspectRatioY != aspectRatioY) {
                mAspectRatioY = aspectRatioY
                mTargetAspectRatio = mAspectRatioX.toFloat() / mAspectRatioY
                if (initializedCropWindow) {
                    initCropWindow()
                    invalidate()
                }
            }
        }

    private var mCropWindowChangeListener: CropWindowChangeListener? = null

    private var mBorderCornerOffset = 0f
    private var mBorderCornerLength = 0f
    private var mBorderCornerPaint: Paint? = null
    private var mGuidelinePaint: Paint? = null
    private var mBackgroundPaint: Paint? = null


    private var mViewWidth = 0
    private var mViewHeight = 0

    private var mMoveHandler: CropRectMoveController? = null

    private var mCenterMoveEnabled = true

    private var mSnapRadius = 0f

    internal companion object {
        // 단순히 지정한 색상만 설정된 Paint 객체를 반환
        internal fun getNewPaint(color: Int): Paint =
            Paint().apply {
                this.color = color
            }
        // 양수일 경우엔 특정 두께와 색상을 갖는 Paint 객체
        internal fun getNewPaintOrNull(thickness: Float, color: Int): Paint? =
            if (thickness > 0) {
                val borderPaint = Paint()
                borderPaint.color = color
                borderPaint.strokeWidth = thickness
                borderPaint.style = Paint.Style.STROKE // 선으로만 그림.
                borderPaint.isAntiAlias = true
                borderPaint
            } else {
                null
            }

        // 색상을 채우는 용도의 Paint 객체
        internal fun getNewPaintWithFill(color: Int): Paint {
            val borderPaint = Paint()
            borderPaint.color = color
            borderPaint.style = Paint.Style.FILL // 면을 채우는 스타일.
            borderPaint.isAntiAlias = true //  부드럽게 채워줌.
            return borderPaint
        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawBorder(canvas)
    }

    private fun drawBorder(canvas: Canvas) {
        if(borderPaint != null){
            val w = borderPaint!!.strokeWidth
            val rect = cropRectController.getRect()
            // 안쪽으로 w/2만큼 땅김.
            Log.i("Deok","그리기 전 1 rect top =  ${rect.top}")
            rect.inset(w/2, w/2)
            Log.i("Deok","그리기 전 2 rect top =  ${rect.top}")

            when(cropShape){
                CropShape.RECTANGLE ->{
                    Log.i("Deok", "drawBorder RECTANGLE")
                    canvas.drawRect(rect, borderPaint!!)
                }

                else -> throw IllegalStateException("Unknown Crop Shape")
            }
        }
    }



    fun setOption(options: CropOptions) {
        val isNotSame = this.options != options
        val isCropWindowChanged = options.fixAspectRatio != this.options?.fixAspectRatio ||
                options.aspectRatioX != this.options?.aspectRatioY ||
                options.aspectRatioY != this.options?.aspectRatioY

        this.options = options

        cropRectController.setMinCropResultSize(options.minCropResultWidth, options.minCropResultHeight)
        cropRectController.setMaxCropResultSize(options.maxCropResultWidth, options.maxCropResultHeight)


        if(!isNotSame){
            return
        }

        cropRectController.setInitialAttributeValues(options)

        mCropCornerRadius = options.cropCornerRadius
        cornerShape = options.cornerShape
        cropShape = options.cropShape
        // 뷰의 속성을 이용해 변경을 허용한다.
        isEnabled = options.canChangeCropWindow
        isFixAspectRatio = options.fixAspectRatio
        aspectRatioX = options.aspectRatioX
        aspectRatioY = options.aspectRatioY
        mTouchRadius = options.touchRadius
        mSnapRadius = options.snapRadius

        mInitialCropWindowPaddingRatio = options.initialCropWindowPaddingRatio
        borderPaint = getNewPaintOrNull(options.borderLineThickness, options.borderLineColor)
        mBorderCornerOffset = options.borderCornerOffset
        mBorderCornerLength = options.borderCornerLength
        mBorderCornerPaint = getNewPaintOrNull(options.borderCornerThickness, options.borderCornerColor)
        mGuidelinePaint = getNewPaintOrNull(options.guidelinesThickness, options.guidelinesColor)
        mBackgroundPaint = getNewPaint(options.backgroundColor)

        Log.i("Deok", "isCropWindowChanged = ${isCropWindowChanged}")

        if(isCropWindowChanged) {
            initCropWindow()
        }

        invalidate()

        if (isCropWindowChanged) {
            mCropWindowChangeListener?.onCropWindowChanged(false)
        }
    }

    fun setCropWindowLimits(
        maxWidth: Float,
        maxHeight: Float,
        scaleFactorWidth: Float,
        scaleFactorHeight: Float,
    ) {
        cropRectController
            .setCropWindowLimits(maxWidth, maxHeight, scaleFactorWidth, scaleFactorHeight)
    }

    fun setBounds(boundsPoints: FloatArray?, viewWidth: Int, viewHeight: Int) {
        if (boundsPoints == null || !mBoundsPoints.contentEquals(boundsPoints)) {
            if (boundsPoints == null) {
                Arrays.fill(mBoundsPoints, 0f)
            } else {
                System.arraycopy(boundsPoints, 0, mBoundsPoints, 0, boundsPoints.size)
            }

            mViewWidth = viewWidth
            mViewHeight = viewHeight
            val cropRect = cropRectController.getRect()
            if (cropRect.width() == 0f || cropRect.height() == 0f) initCropWindow()
        }
    }


    private fun initCropWindow() {
        // 4군데 좌표로부터 크기 제한을 얻음.
        val leftLimit = max(BitmapUtils.getRectLeft(mBoundsPoints), 0f)
        val topLimit = max(BitmapUtils.getRectTop(mBoundsPoints), 0f)
        val rightLimit = min(BitmapUtils.getRectRight(mBoundsPoints), width.toFloat())
        val bottomLimit = min(BitmapUtils.getRectBottom(mBoundsPoints), height.toFloat())

        Log.i("Deok","leftLimit = ${leftLimit} topLimit = ${topLimit} rightLimit = ${rightLimit} bottomLimit= ${bottomLimit}  ")

        if (rightLimit <= leftLimit || bottomLimit <= topLimit) return

        val rect = RectF()

        initializedCropWindow = true
        val horizontalPadding = mInitialCropWindowPaddingRatio * (rightLimit - leftLimit)
        val verticalPadding = mInitialCropWindowPaddingRatio * (bottomLimit - topLimit)


        if (mInitialCropWindowRect.width() > 0 && mInitialCropWindowRect.height() > 0) {
            Log.i("Deok", "mInitialCropWindowRect.width() > 0 && mInitialCropWindowRect.height() > 0")
            // 진열된 이미지와 상대적인 크롭 윈도우 좌표를 얻습니다.
            // leftLimit - 이미지가 시작되는 지점
            // mInitialCropWindowRect.left - 이미지가 시작되는 지점에서 얼마나 오른쪽으로 떨어져있는지.
            // rect.left = 스마트폰 화면에서 왼쪽 기준으로 얼마나 떨어져있는지.
            rect.left =
                leftLimit + mInitialCropWindowRect.left / cropRectController.getScaleFactorWidth()
            rect.top =
                topLimit + mInitialCropWindowRect.top / cropRectController.getScaleFactorHeight()
            rect.right =
                rect.left + mInitialCropWindowRect.width() / cropRectController.getScaleFactorWidth()
            rect.bottom =
                rect.top + mInitialCropWindowRect.height() / cropRectController.getScaleFactorHeight()

            // 이미지 바깥으로 나가지 않도록 경계 보정
            rect.left = max(leftLimit, rect.left)
            rect.top = max(topLimit, rect.top)
            rect.right = min(rightLimit, rect.right)
            rect.bottom = min(bottomLimit, rect.bottom)

            // 고정 비율이 설정된 경우
        } else if (isFixAspectRatio && rightLimit > leftLimit && bottomLimit > topLimit) {
            Log.i("Deok", "고정 비율이 설정된 경우")

            val bitmapAspectRatio = (rightLimit - leftLimit) / (bottomLimit - topLimit)

            // 이해는 했지만, 어떠한 목적일까
            // 이미지가 더 넓을 경우
            if (bitmapAspectRatio > mTargetAspectRatio) {
                // 이미지의 세로에 맞춰서 크롭 창 크기 결정
                rect.top = topLimit + verticalPadding
                rect.bottom = bottomLimit - verticalPadding
                val centerX = width / 2f

                mTargetAspectRatio = mAspectRatioX.toFloat() / mAspectRatioY

                val cropWidth = max(
                    cropRectController.getMinCropWidth(),
                    rect.height() * mTargetAspectRatio,
                )
                val halfCropWidth = cropWidth / 2f
                rect.left = centerX - halfCropWidth
                rect.right = centerX + halfCropWidth
            }
            // 이미지가 더 높을 경우
            else {

                // 이미지의 가로에 맞춰서 크롭 창 크기 결정
                rect.left = leftLimit + horizontalPadding
                rect.right = rightLimit - horizontalPadding
                val centerY = height / 2f
                // Limits the aspect ratio to no less than 40 wide or 40 tall
                val cropHeight = max(
                    cropRectController.getMinCropHeight(),
                    rect.width() / mTargetAspectRatio,
                )
                val halfCropHeight = cropHeight / 2f
                rect.top = centerY - halfCropHeight
                rect.bottom = centerY + halfCropHeight
            }
        } else {
            Log.i("Deok", "이도저도 아닌경우")
            rect.left = leftLimit + horizontalPadding
            rect.top = topLimit + verticalPadding
            rect.right = rightLimit - horizontalPadding
            rect.bottom = bottomLimit - verticalPadding
        }

        Log.i("Deok", "1 rect ${rect.left} ${rect.top} ${rect.right} ${rect.bottom}")
        fixCropWindowRectByRules(rect)

        Log.i("Deok", "5 rect ${rect.left} ${rect.top} ${rect.right} ${rect.bottom}")
        cropRectController.setRect(rect)
    }

    // 크롭 윈도우(rect)가 최소/최대 크기, 이미지 경계, 그리고 고정된 비율(aspect ratio) 조건을 만족하도록 자동으로 보정(fix) 하는 역할
    private fun fixCropWindowRectByRules(rect: RectF) {
        // 너비가 최소값보다 작으면,
        //
        //왼쪽을 왼쪽으로, 오른쪽을 오른쪽으로 adj만큼 늘려서 너비를 최소값까지 확장
        if (rect.width() < cropRectController.getMinCropWidth()) {
            val adj = (cropRectController.getMinCropWidth() - rect.width()) / 2
            rect.left -= adj
            rect.right += adj
        }

        if (rect.height() < cropRectController.getMinCropHeight()) {
            val adj = (cropRectController.getMinCropHeight() - rect.height()) / 2
            rect.top -= adj
            rect.bottom += adj
        }

        if (rect.width() > cropRectController.getMaxCropWidth()) {
            val adj = (rect.width() - cropRectController.getMaxCropWidth()) / 2
            rect.left += adj
            rect.right -= adj
        }

        if (rect.height() > cropRectController.getMaxCropHeight()) {
            val adj = (rect.height() - cropRectController.getMaxCropHeight()) / 2
            rect.top += adj
            rect.bottom -= adj
        }

        Log.i("Deok", "2 rect ${rect.left} ${rect.top} ${rect.right} ${rect.bottom}")

        calculateBounds()

        Log.i("Deok", "3 rect ${rect.left} ${rect.top} ${rect.right} ${rect.bottom}")
        // rect가 mCalcBounds 범위 내에 있고, 화면 밖으로 나가지 않도록 제한하는 코드
        if (mCalcBounds.width() > 0 && mCalcBounds.height() > 0) {
            val leftLimit = max(mCalcBounds.left, 0f)
            val topLimit = max(mCalcBounds.top, 0f)
            val rightLimit = min(mCalcBounds.right, width.toFloat())
            val bottomLimit = min(mCalcBounds.bottom, height.toFloat())

            if (rect.left < leftLimit) rect.left = leftLimit
            if (rect.top < topLimit) rect.top = topLimit
            if (rect.right > rightLimit) rect.right = rightLimit
            if (rect.bottom > bottomLimit) rect.bottom = bottomLimit
        }

        // rect의 비율을 mTargetAspectRatio(=너비/높이)에 맞추는 코드
        if (isFixAspectRatio && abs(rect.width() - rect.height() * mTargetAspectRatio) > 0.1) {
            if (rect.width() > rect.height() * mTargetAspectRatio) {
                val adj = abs(rect.height() * mTargetAspectRatio - rect.width()) / 2
                rect.left += adj
                rect.right -= adj
            } else {
                val adj = abs(rect.width() / mTargetAspectRatio - rect.height()) / 2
                rect.top += adj
                rect.bottom -= adj
            }
        }

        Log.i("Deok", "4 rect ${rect.left} ${rect.top} ${rect.right} ${rect.bottom}")
    }



    // 경계를 계산하는 것.
    private fun calculateBounds(): Boolean {
        var left = BitmapUtils.getRectLeft(mBoundsPoints)
        var top = BitmapUtils.getRectTop(mBoundsPoints)
        var right = BitmapUtils.getRectRight(mBoundsPoints)
        var bottom = BitmapUtils.getRectBottom(mBoundsPoints)

        // 직선적인 각도(0도, 90도, 180도, 270도) 로 회전되었거나 회전되지 않은 상태입니다.
        Log.i("Deok", "snapEdgesToBounds calculateBounds mCalcBounds.right = ${mCalcBounds.right}")
        mCalcBounds[left, top, right] = bottom
        return false
    }

    fun resetCropOverlayView() {
        if (initializedCropWindow) {
            cropWindowRect = BitmapUtils.EMPTY_RECT_F
            initCropWindow()
            invalidate()
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 뷰가 입력을 받을 활성화상태인지 검사.
        return if (isEnabled) {
            // mScaleDetector를 사용하여 멀티터치 이벤트를 처리합니다. mScaleDetector는 스케일링 제스처(예: 핀치 줌) 등을 감지하는 객체로 추측

            when (event.action) {
                // ACTION_DOWN (터치 시작)
                MotionEvent.ACTION_DOWN -> {
                    Log.i("Deok", "ACTION_DOWN START")
                    // 터치 시작 위치에서 필요한 처리를 함
                    onActionDown(event.x, event.y)
                    Log.i("Deok", "ACTION_DOWN END")
                    true
                }
                // 터치가 이동하는 동안 발생하는 이벤트
                MotionEvent.ACTION_MOVE -> {
                    Log.i("Deok", "ACTION_MOVE START")
                    // 터치 이동에 대한 처리
                    onActionMove(event.x, event.y)
                    // 부모 뷰가 이 터치 이벤트를 가로채지 않도록 설정
                    // 드래그 중에 부모 뷰가 터치이벤트를 가로채지 않도록 하기 위한 설정.
                    parent.requestDisallowInterceptTouchEvent(true)
                    Log.i("Deok", "ACTION_MOVE END")
                    true

                }
                // ACTION_UP 또는 ACTION_CANCEL (터치 종료)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    Log.i("Deok", "ACTION_UP 또는 ACTION_CANCEL START")
                    // 부모 뷰가 터치 이벤트를 가로채지 않도록 설정
                    parent.requestDisallowInterceptTouchEvent(false)
                    onActionUp()
                    Log.i("Deok", "ACTION_UP 또는 ACTION_CANCEL END")
                    true
                }
                else -> false
            }
        } else {
            false
        }
    }

    private fun onActionDown(x: Float, y: Float) {
        Log.i("Deok", "터치 지점 x = ${x} y = ${y}")

        mMoveHandler =
                // 크롭 창의 테두리나 코너, 혹은 안쪽 중앙에 있는지를 판단해서,
                // 움직일 수 있는 핸들(MoveHandler) 객체를 반환
            cropRectController.getMoveHandler(x, y, mTouchRadius, cropShape!!, mCenterMoveEnabled)

        if (mMoveHandler != null) invalidate()
    }

    // 크롭 영역을 이동하거나 크기 조절하는 역할
    private fun onActionMove(x: Float, y: Float) {
        Log.i("Deok", "Move 지점 x = ${x} y = ${y}")
        if (mMoveHandler != null) {
            var snapRadius = mSnapRadius
            val rect = cropRectController.getRect()
            Log.i("Deok", "snapEdgesToBounds onActionMove")
            calculateBounds()


            // 실제로 크롭 영역을 이동하거나 크기 조절하는 핵심 로직 호출
            mMoveHandler!!.move(
                rect, // 현재 크롭 위치
                x, // 사용자의 손가락 위치
                y, // 사용자의 손가락 위치
                mCalcBounds, // 크롭 가능한 범위
                mViewWidth, // 전체 이미지 뷰 크기
                mViewHeight, // 전체 이미지 뷰 크기
                snapRadius, //  가장자리 붙임 기능 반경
                isFixAspectRatio, // 비율 고정 여부
                mTargetAspectRatio, // 고정할 비율
            )
            // 이동/크기 변경된 후의 rect를 crop window에 반영
            cropRectController.setRect(rect)
            // 크롭 영역이 변경 중임을 외부에 알림 (true)
            mCropWindowChangeListener?.onCropWindowChanged(true)
            // 이동된 rect를 반영하기위하여 다시 그리기 호출
            invalidate()
        }
    }

    private fun onActionUp() {
        // 사용자가 크롭 영역을 조작하고 있었는지를 확인
        if (mMoveHandler != null) {
            // 더 이상 조작 중이 아니므로, 조작 핸들을 제거
            mMoveHandler = null
            // 크롭 창 변경이 종료되었음을 외부에 알림
            mCropWindowChangeListener?.onCropWindowChanged(false)
            // 화면 다시 그리기 요청
            invalidate()
        }
    }

    fun setCropWindowChangeListener(listener: CropWindowChangeListener?) {
        mCropWindowChangeListener = listener
    }
}

internal fun interface CropWindowChangeListener {
    fun onCropWindowChanged(inProgress: Boolean)
}

enum class CropShape {
    RECTANGLE,
}

enum class CropCornerShape {
    RECTANGLE
}

enum class ScaleType {
    FIT_CENTER,
    CENTER,
    CENTER_CROP,
    CENTER_INSIDE
}