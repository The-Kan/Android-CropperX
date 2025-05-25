package com.devyd.cropperx.crop

import android.content.res.Resources
import android.graphics.Color
import android.os.Parcelable
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.Px
import com.devyd.cropperx.view.CropCornerShape
import com.devyd.cropperx.view.CropShape
import kotlinx.parcelize.Parcelize

@Parcelize
data class CropOptions (
    // Aspect ratio settings
    val fixAspectRatio: Boolean = false,
    val aspectRatioX: Int = 1,
    val aspectRatioY: Int = 1,

    // Border line properties
    @Px val borderLineThickness: Float = dpToPx(3f),
    @ColorInt val borderLineColor: Int = Color.argb(190, 0, 0, 0),

    // Minimum crop window dimensions in view-space (px)
    @Px val minCropWindowWidth: Int = dpToPx(42f).toInt(),
    @Px val minCropWindowHeight: Int = dpToPx(42f).toInt(),

    // Result image constraints in image-space (px)
    @Px val minCropResultWidth: Int = 40,
    @Px val minCropResultHeight: Int = 40,
    @Px val maxCropResultWidth: Int = Int.MAX_VALUE,
    @Px val maxCropResultHeight: Int = Int.MAX_VALUE,

    // Corner styling
    @Px val cropCornerRadius: Float = dpToPx(10f),
    val cornerShape: CropCornerShape = CropCornerShape.RECTANGLE,

    // Crop shape (rectangle, oval, etc.)
    val cropShape: CropShape = CropShape.RECTANGLE,

    // Interactivity
    val canChangeCropWindow: Boolean = true,

    // Padding ratio for initial crop window (0f = no padding)
    val initialCropWindowPaddingRatio: Float = 0.0f,

    // Touch area radius around handles
    @Px val touchRadius: Float = dpToPx(24f),

    // Corner indicator styling
    @Px val borderCornerOffset: Float = dpToPx(5f),
    @Px val borderCornerLength: Float = dpToPx(14f),
    @Px val borderCornerThickness: Float = dpToPx(2f),
    @ColorInt val borderCornerColor: Int = Color.WHITE,

    // Guidelines inside crop window
    @Px val guidelinesThickness: Float = dpToPx(1f),
    @ColorInt val guidelinesColor: Int = Color.argb(190, 0, 0, 0),

    // Overlay background shading
    @ColorInt val backgroundColor: Int = Color.argb(119, 0, 0, 0)
) : Parcelable {
    companion object {
        private fun dpToPx(dp: Float): Float =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                Resources.getSystem().displayMetrics
            )
    }
}