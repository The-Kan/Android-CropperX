package com.devyd.androidcropper.navigation

import android.graphics.Bitmap
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.devyd.androidcropper.navigation.screens.cropperx.CropperX
import com.devyd.androidcropper.navigation.screens.selectimage.SelectImage
import com.devyd.androidcropper.navigation.screens.showimage.ShowImage
import com.devyd.androidcropper.state.ShowImageState
import com.devyd.androidcropper.util.ImmutableBitmap
import com.devyd.androidcropper.viewmodel.NaviViewModel

@Composable
fun CropNavigation() {

    val navController = rememberNavController()
    val naviViewModel = hiltViewModel<NaviViewModel>()

    val navigateSelectImage = remember<() -> Unit> {
        {
            naviViewModel.reset()
            navController.navigate(NavList.SELECT_IMAGE)
        }
    }

    val navigateCropperX = remember<(ShowImageState) -> Unit> {
        {
            naviViewModel.updateStacksFromShowImageState(it)
            navController.navigate(NavList.CROPPER_X)
        }
    }

    val navigateBackPress = remember<() -> Unit> {
        { navController.navigateUp() }
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
            SelectImage(onImageLoaded = onImageLoaded)
        }

        composable(
            route = NavList.SHOW_IMAGE
        ) {
            ShowImage(
                initialState = ShowImageState(naviViewModel.undoStack, naviViewModel.redoStack),
                navigateCropperX = navigateCropperX,
                navigateBackPress = navigateBackPress,
            )
        }

        composable(
            route = NavList.CROPPER_X
        ) {
            CropperX(
                immutableBitmap = ImmutableBitmap(naviViewModel.getCurrentBitmap()),
                onDoneClicked = onDoneClicked,
                navigateBackPress =  navigateBackPress
            )
        }


    }


}