package com.devyd.androidcropper.navigation

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.devyd.androidcropper.R
import com.devyd.androidcropper.navigation.screens.cropperx.CropperX
import com.devyd.androidcropper.navigation.screens.selectimage.SelectImage
import com.devyd.androidcropper.navigation.screens.showimage.ShowImage
import com.devyd.androidcropper.state.ShowImageState
import com.devyd.androidcropper.util.AppUtil
import com.devyd.androidcropper.util.BitmapUtil
import com.devyd.androidcropper.util.FileUtil
import com.devyd.androidcropper.util.ImmutableBitmap
import com.devyd.androidcropper.util.toast
import com.devyd.androidcropper.viewmodel.NaviViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CropNavigation() {

    val navController = rememberNavController()
    val naviViewModel = hiltViewModel<NaviViewModel>()

    val navigateSelectImage = remember<() -> Unit> {
        {
            naviViewModel.reset()
            navController.navigate(
                NavList.SELECT_IMAGE,
                navOptions = NavOptions.Builder()
                    .setPopUpTo(route = NavList.SELECT_IMAGE, inclusive = true)
                    .build()
            )
        }
    }

    val navigateCropperX = remember<(ShowImageState) -> Unit> {
        {
            naviViewModel.updateStacksFromShowImageState(it)
            navController.navigate(NavList.CROPPER_X)
        }
    }

    val navigateBackPress = remember<() -> Unit> {
        {
            navController.navigateUp()
        }
    }


    val onImageLoaded = remember<(Bitmap) -> Unit> {
        {
            naviViewModel.addToStack(
                bitmap = it.copy(Bitmap.Config.ARGB_8888, false)
            )
            navController.navigate(NavList.SHOW_IMAGE)
        }
    }

    val onDoneClicked = remember<(Bitmap) -> Unit> {
        {
                naviViewModel.addToStack(
                    bitmap = it.copy(Bitmap.Config.ARGB_8888, false)
                )
                navController.navigate(
                    NavList.SHOW_IMAGE,
                    navOptions = NavOptions.Builder()
                        .setPopUpTo(route = NavList.SHOW_IMAGE, inclusive = true)
                        .build()
                )
        }
    }

    val onSaveInSampleSize = remember<(Int) -> Unit> {
        {
           naviViewModel.setInSampleSize(it)
        }
    }

    val onSaveOriginalImageUri = remember<(Uri) -> Unit> {
        {
            naviViewModel.setOriginalImageUri(it)
        }
    }


    NavHost(
        navController = navController,
        startDestination = NavList.SELECT_IMAGE,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {

        composable(
            route = NavList.SELECT_IMAGE
            // 애니 설정 필요
        ) {
            SelectImage(
                onImageLoaded = onImageLoaded,
                onSaveInSampleSize = onSaveInSampleSize,
                onSaveOriginalImageUri = onSaveOriginalImageUri,
            )
        }

        composable(
            route = NavList.SHOW_IMAGE
        ) {
            ShowImage(
                initialState = ShowImageState(naviViewModel.undoStack, naviViewModel.redoStack),
                navigateCropperX = navigateCropperX,
                navigateSelectImage = navigateSelectImage,
            )
        }

        composable(
            route = NavList.CROPPER_X
        ) {
            CropperX(
                immutableBitmap = ImmutableBitmap(naviViewModel.getCurrentBitmap()),
                onDoneClicked = onDoneClicked,
                navigateBackPress = navigateBackPress,
                inSampleSize = naviViewModel.inSampleSize,
                originalImageUri = naviViewModel.originalImageUri
            )
        }


    }


}