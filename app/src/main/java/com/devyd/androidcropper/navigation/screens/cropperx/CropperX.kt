package com.devyd.androidcropper.navigation.screens.cropperx

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.devyd.androidcropper.R
import com.devyd.androidcropper.navigation.screens.common.AnimatedToolbar
import com.devyd.androidcropper.navigation.screens.common.BottomToolbarHeightPaddingInEdgeToEdge
import com.devyd.androidcropper.navigation.screens.common.BottomToolbarModifier
import com.devyd.androidcropper.navigation.screens.common.TopToolbarHeightPaddingInEdgeToEdge
import com.devyd.androidcropper.navigation.screens.common.TopToolbarModifier
import com.devyd.androidcropper.navigation.screens.cropperx.bottomtoolbar.CropperXBottomToolBar
import com.devyd.androidcropper.navigation.screens.cropperx.toptoolbar.CropperXTopToolBar
import com.devyd.androidcropper.util.AniUtil
import com.devyd.androidcropper.util.AppUtil
import com.devyd.androidcropper.util.BitmapUtil
import com.devyd.androidcropper.util.FileUtil
import com.devyd.androidcropper.util.ImmutableBitmap
import com.devyd.androidcropper.util.SizeUtil
import com.devyd.androidcropper.util.getActivity
import com.devyd.androidcropper.util.toast
import com.devyd.cropperx.crop.CropOptions
import com.devyd.cropperx.view.CropperXView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max
import kotlin.math.min

@Composable
fun CropperX(
    immutableBitmap: ImmutableBitmap,
    onDoneClicked: (Bitmap) -> Unit,
    navigateBackPress: () -> Unit,
    inSampleSize : Int,
    originalImageUri : Uri?,
) {

    val context = LocalContext.current
    val activity = LocalContext.current.getActivity()
    val lifecycleOwner = LocalLifecycleOwner.current



    val topToolbarHeight = SizeUtil.TOOLBAR_HEIGHT_SMALL
    val bottomToolbarHeight = SizeUtil.TOOLBAR_HEIGHT_X_LARGE


    var toolbarVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        toolbarVisible = true
    }

    val previousSoftInputMode = remember {
        activity?.window?.attributes?.softInputMode
            ?: WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED
    }

    DisposableEffect(Unit) {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        onDispose {
            activity?.window?.setSoftInputMode(previousSoftInputMode)
        }
    }


    var neededCrop by remember { mutableStateOf(false) }
    var selectedOptionIdx by remember { mutableStateOf(0) }
    var cropOptions by remember {
        mutableStateOf(CropOptions())
    }


    val onCloseClicked = remember<() -> Unit> {
        {
            lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                toolbarVisible = false
                delay(AniUtil.TOOLBAR_COLLAPSE_ANIM_DURATION.toLong())
                navigateBackPress()
            }
        }
    }

    BackHandler {
        onCloseClicked()
    }


    val onCropperXDoneClicked = remember<() -> Unit> {
        {
            neededCrop = true
        }
    }

    val handleCropResult = remember<(Bitmap) -> Unit> {
        {
            lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                if (AppUtil.isOriginalEditSupported()){
                    withContext(Dispatchers.IO){
                        val imageFile = File(context.filesDir, "cropped_image.jpg")
                        BitmapUtil.saveBitmapToFile(it, imageFile)

                        FileUtil.saveFileToGallery(
                            context = context,
                            file = imageFile,
                            onSuccess = {
                                lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                                    context.toast(R.string.image_saved_in_gallery_app)
                                }

                            },
                            onFail = {
                                lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                                    context.toast(R.string.failed_to_save_image)
                                }
                            }
                        )
                    }
                } else {
                    toolbarVisible = false
                    delay(AniUtil.TOOLBAR_COLLAPSE_ANIM_DURATION.toLong())
                    onDoneClicked(it)
                }
            }
        }
    }

    val cropCompleteListener = remember {
        CropperXView.OnCropCompleteListener { view, result ->
            result.bitmap?.let {
                handleCropResult(it)
            }
        }
    }


    val onCropOptionClicked = remember<(Int, CropOption) -> Unit> {
        { idx, cropOption ->

            selectedOptionIdx = idx
            when (cropOption.aspectRatioX) {
                -1f -> {
                    // free
                    cropOptions = cropOptions.copy(
                        fixAspectRatio = false,
                        aspectRatioX = 1,
                        aspectRatioY = 1
                    )
                }

                else -> {
                    // fixRatio
                    cropOptions = cropOptions.copy(
                        fixAspectRatio = true,
                        aspectRatioX = cropOption.aspectRatioX.toInt(),
                        aspectRatioY = cropOption.aspectRatioY.toInt()
                    )
                }
            }
        }
    }

    val onCropVerticalHorizontalClicked = remember<(Boolean) -> Unit> {
        { isVertical ->
            val tmpCropOptions = cropOptions.copy(
                aspectRatioX = if (isVertical) min(
                    cropOptions.aspectRatioX,
                    cropOptions.aspectRatioY
                ) else max(cropOptions.aspectRatioX, cropOptions.aspectRatioY),
                aspectRatioY = if (isVertical) max(
                    cropOptions.aspectRatioX,
                    cropOptions.aspectRatioY
                ) else min(cropOptions.aspectRatioX, cropOptions.aspectRatioY)
            )

            if (tmpCropOptions != cropOptions) cropOptions = tmpCropOptions
        }
    }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val (topToolBar, cropperX, bottomToolBar) = createRefs()

        AnimatedToolbar(
            visible = toolbarVisible,
            modifier = TopToolbarModifier(topToolBar)
        ) {
            TopToolbarHeightPaddingInEdgeToEdge {
                CropperXTopToolBar(
                    modifier = Modifier,
                    toolbarHeight = topToolbarHeight,
                    onCloseClicked = onCloseClicked,
                    onDoneClicked = onCropperXDoneClicked
                )
            }
        }

        Box(
            modifier = Modifier
                .constrainAs(cropperX) {
                    width = Dimension.matchParent
                    height = Dimension.matchParent
                }
                .padding(top = topToolbarHeight, bottom = bottomToolbarHeight)
        ) {
            AndroidView(
                modifier = Modifier,
                factory = { context ->
                    CropperXView(context = context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        if(AppUtil.isOriginalEditSupported()){
                            setImageBitmap(immutableBitmap.bitmap, originalImageUri, inSampleSize)
                        } else {
                            setImageBitmap(immutableBitmap.bitmap)
                        }
                        setImageCropOptions(cropOptions)
                        setOnCropImageCompleteListener(cropCompleteListener)

                    }
                },
                update = { cropperXView ->

                    if (neededCrop) {
                        // 크롭 비동기 시작
                        cropperXView.startCropImage()
                    }
                    cropperXView.setImageCropOptions(cropOptions)
                }
            )

        }


        AnimatedToolbar(
            visible = toolbarVisible,
            modifier = BottomToolbarModifier(bottomToolBar)
        ) {
            BottomToolbarHeightPaddingInEdgeToEdge {
                CropperXBottomToolBar(
                    modifier = Modifier,
                    toolbarHeight = bottomToolbarHeight,
                    selectedOptionIdx = selectedOptionIdx,
                    onCropOptionClicked = onCropOptionClicked,
                    onCropVerticalHorizontalClicked = onCropVerticalHorizontalClicked
                )
            }

        }

    }


}