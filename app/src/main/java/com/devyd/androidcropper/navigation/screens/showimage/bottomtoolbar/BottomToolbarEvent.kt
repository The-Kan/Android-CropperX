package com.devyd.androidcropper.navigation.screens.showimage.bottomtoolbar

sealed class BottomToolbarEvent {
    data class OnItemClicked(val toolbarItem : BottomToolbarItem) : BottomToolbarEvent()
}