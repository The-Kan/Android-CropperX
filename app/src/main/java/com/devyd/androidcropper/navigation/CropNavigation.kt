package com.devyd.androidcropper.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.devyd.androidcropper.navigation.screens.cropperx.CropperX
import com.devyd.androidcropper.navigation.screens.selectimage.SelectImage
import com.devyd.androidcropper.navigation.screens.showimage.ShowImage

@Composable
fun CropNavigation() {

    val navController = rememberNavController()

    val navigateSelectImage = remember {
        { navController.navigate(NavList.SELECT_IMAGE) }
    }

    val navigateShowImage = remember {
        { navController.navigate(NavList.SHOW_IMAGE) }
    }

    val navigateCropperX = remember {
        { navController.navigate(NavList.CROPPER_X) }
    }

    val navigateBackPress = remember {
        { navController.navigateUp() }
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
            SelectImage()
        }

        composable(
            route = NavList.SHOW_IMAGE
        ) {
            ShowImage()
        }

        composable(
            route = NavList.CROPPER_X
        ) {
            CropperX()
        }


    }


}