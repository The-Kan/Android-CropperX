package com.devyd.androidcropper.navigation.screens.showimage

import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavBackStackEntry
import com.devyd.androidcropper.R
import com.devyd.androidcropper.navigation.screens.common.AnimatedToolbar
import com.devyd.androidcropper.navigation.screens.common.BottomToolbarHeightPaddingInEdgeToEdge
import com.devyd.androidcropper.navigation.screens.common.BottomToolbarModifier
import com.devyd.androidcropper.navigation.screens.common.TopToolbarHeightPaddingInEdgeToEdge
import com.devyd.androidcropper.navigation.screens.common.TopToolbarModifier
import com.devyd.androidcropper.navigation.screens.showimage.bottomtoolbar.BottomToolbarEvent
import com.devyd.androidcropper.navigation.screens.showimage.bottomtoolbar.BottomToolbarItem
import com.devyd.androidcropper.navigation.screens.showimage.bottomtoolbar.BottomToolbarDefaultItemList
import com.devyd.androidcropper.navigation.screens.showimage.bottomtoolbar.ShowImageBottomToolBar
import com.devyd.androidcropper.navigation.screens.showimage.toptoolbar.ShowImageTopToolBar
import com.devyd.androidcropper.state.ShowImageState
import com.devyd.androidcropper.util.AniUtil
import com.devyd.androidcropper.util.BitmapUtil
import com.devyd.androidcropper.util.FileUtil
import com.devyd.androidcropper.util.ImmutableList
import com.devyd.androidcropper.util.LogUtil
import com.devyd.androidcropper.util.SizeUtil
import com.devyd.androidcropper.util.toast
import com.devyd.androidcropper.viewmodel.ShowImageViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


@Composable
fun ShowImage(
    modifier: Modifier = Modifier,
    initialState: ShowImageState,
    navigateCropperX: (ShowImageState) -> Unit,
    navigateSelectImage: () -> Unit,
) {
    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            Log.i("Deok", "ON_RESUME")
        }
    }
    LocalLifecycleOwner.current.lifecycle.addObserver(observer)


    val viewModel: ShowImageViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        viewModel.initState(initialState)
    }

    val state by viewModel.state.collectAsStateWithLifecycle()


    if (state.undoStack.isNotEmpty()) {

        val bitmap = viewModel.getCurrentBitmap()

        val bottomToolbarEvent = remember<(BottomToolbarEvent) -> Unit> {
            {
                if (it is BottomToolbarEvent.OnItemClicked) {
                    when (it.toolbarItem) {
                        BottomToolbarItem.Crop -> {
                            navigateCropperX(state)
                        }
                    }
                }
            }
        }


        ShowImageLayout(
            modifier = modifier,
            bitmap = bitmap,
            bottomToolbarEvent = bottomToolbarEvent,
            navigateSelectImage = navigateSelectImage,
            isUndoPossible = viewModel.isUndoPossible(),
            isRedoPossible = viewModel.isRedoPossible(),
            undo = viewModel::undo,
            redo = viewModel::redo,
        )
    }


}

@Composable
fun ShowImageLayout(
    modifier: Modifier,
    bitmap: Bitmap,
    bottomToolbarEvent: (BottomToolbarEvent) -> Unit,
    navigateSelectImage: () -> Unit,
    isUndoPossible: Boolean,
    isRedoPossible: Boolean,
    undo: () -> Unit,
    redo: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current // navigation backstackEntry

    val onCloseClicked = remember<() -> Unit> {
        {
            navigateSelectImage()
        }
    }

    BackHandler {
        navigateSelectImage()
    }

    val topToolbarHeight = SizeUtil.TOOLBAR_HEIGHT_SMALL
    val bottomToolbarHeight = SizeUtil.TOOLBAR_HEIGHT_MEDIUM

    val onSaveClicked = remember<() -> Unit> {
        {
            lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val imageFile = File(context.filesDir, "cropped_image.jpg")
                BitmapUtil.saveBitmapToFile(bitmap, imageFile)

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
        }
    }


    val bottomToolbarItemList = remember {
        ImmutableList(BottomToolbarDefaultItemList.getList())
    }


    var toolbarVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { toolbarVisible = true }


    val bottomToolbarEventWithAnim = remember<(BottomToolbarEvent) -> Unit> {
        {
            if (it is BottomToolbarEvent.OnItemClicked) {
                lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    toolbarVisible = false
                    delay(AniUtil.TOOLBAR_COLLAPSE_ANIM_DURATION.toLong())
                    bottomToolbarEvent(it)
                }
            }
        }
    }



    ConstraintLayout(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val (topToolbar, bottomToolbar, image) = createRefs()

        AnimatedToolbar(
            modifier = TopToolbarModifier(topToolbar),
            visible = toolbarVisible
        )
        {
            TopToolbarHeightPaddingInEdgeToEdge {
                ShowImageTopToolBar(
                    modifier = Modifier,
                    isUndoPossible = isUndoPossible,
                    isRedoPossible = isRedoPossible,
                    isSavePossible = isUndoPossible,
                    undo = undo,
                    redo = redo,
                    save = onSaveClicked,
                    close = onCloseClicked,
                    toolBarHeight = topToolbarHeight,
                )
            }
        }

        val aspectRatio = remember(bitmap) {
            bitmap.width.toFloat() / bitmap.height.toFloat()
        }

        val topDP = with(LocalDensity.current) {
            WindowInsets.statusBars.getTop(this).toDp()
        }

        val topPadding = topToolbarHeight + topDP

        val bottomDp = with(LocalDensity.current) {
            WindowInsets.navigationBars.getBottom(this).toDp()
        }

        val bottomPadding = bottomToolbarHeight + bottomDp

        Box(
            modifier = Modifier
                .constrainAs(image) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.wrapContent
                    height = Dimension.wrapContent
                }
                .padding(top = topPadding, bottom = bottomPadding)
                .aspectRatio(aspectRatio)
        ) {
            Image(
                modifier = Modifier.fillMaxSize(),
                bitmap = bitmap.asImageBitmap(),
                contentScale = ContentScale.Fit,
                contentDescription = null,
                alpha = 1f
            )
        }

        AnimatedToolbar(
            modifier = BottomToolbarModifier(bottomToolbar),
            visible = toolbarVisible
        ) {
            BottomToolbarHeightPaddingInEdgeToEdge {
                ShowImageBottomToolBar(
                    modifier = Modifier,
                    bottomToolbarItemList = bottomToolbarItemList,
                    bottomToolbarHeight = bottomToolbarHeight,
                    bottomToolbarEvent = bottomToolbarEventWithAnim
                )
            }
        }

    }
}