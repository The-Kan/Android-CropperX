package com.devyd.androidcropper.navigation.screens.cropperx.bottomtoolbar

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devyd.androidcropper.navigation.screens.cropperx.CropOption
import com.devyd.androidcropper.util.CropUtil
import com.devyd.androidcropper.util.SizeUtil

@SuppressLint("MutableCollectionMutableState")
@Composable
fun CropperXBottomToolBar(
    modifier: Modifier = Modifier,
    toolbarHeight: Dp = SizeUtil.TOOLBAR_HEIGHT_X_LARGE,
    selectedOptionIdx: Int,
    onCropOptionClicked: (Int, CropOption) -> Unit,
    onCropVerticalHorizontalClicked : (isVertical: Boolean) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(toolbarHeight)
            .padding(8.dp)
    ) {

        var cropOptionList by remember { mutableStateOf(CropUtil.getCropVerticalRatioList()) }


        val option = CropUtil.getCropVerticalRatioList()[selectedOptionIdx]


        var verticalHorizontalClickable by remember { mutableStateOf(option.aspectRatioX != option.aspectRatioY) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(SizeUtil.TOOLBAR_HEIGHT_SMALL),
            horizontalArrangement = Arrangement.Center
        ) {

            var isVertical by remember { mutableStateOf(true) }

            var verticalShapeModifier = Modifier
                .aspectRatio(9 / 16f)
                .clip(RoundedCornerShape(2.dp))
                .background(color = MaterialTheme.colorScheme.onBackground)
                .padding(1.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.background)

            var horizontalShapeModifier = Modifier
                .padding(vertical = 10.dp)
                .aspectRatio(16 / 9f)
                .clip(RoundedCornerShape(2.dp))
                .background(color = MaterialTheme.colorScheme.onBackground)
                .padding(1.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.background)

            if (verticalHorizontalClickable) {
                if(isVertical){
                    horizontalShapeModifier = horizontalShapeModifier.clickable {
                        isVertical = false
                        cropOptionList = CropUtil.getCropHorizontalRatioList()
                        onCropVerticalHorizontalClicked(isVertical)
                    }
                } else {
                    verticalShapeModifier = verticalShapeModifier.clickable {
                        isVertical = true
                        cropOptionList = CropUtil.getCropVerticalRatioList()
                        onCropVerticalHorizontalClicked(isVertical)
                    }
                }


            }

            Box(
                modifier = verticalShapeModifier,
                contentAlignment = Alignment.Center
            ) {
                if (verticalHorizontalClickable && isVertical) {
                    Image(
                        modifier = Modifier
                            .size(15.dp),
                        contentDescription = null,
                        imageVector = Icons.Default.Check,
                        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground)
                    )
                }

            }

            Spacer(modifier = Modifier.padding(10.dp))

            Box(
                modifier = horizontalShapeModifier,
                contentAlignment = Alignment.Center
            ) {
                if (verticalHorizontalClickable && !isVertical) {
                    Image(
                        modifier = Modifier
                            .size(15.dp),
                        contentDescription = null,
                        imageVector = Icons.Default.Check,
                        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground)
                    )
                }
            }
        }


        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(SizeUtil.TOOLBAR_HEIGHT_MEDIUM)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(
                count = cropOptionList.size,
                key = { cropOptionList[it].id }) { idx ->
                val cropOption = cropOptionList[idx]
                CropOptionView(
                    modifier = Modifier
                        .padding(horizontal = 4.dp),
                    cropOption = cropOption,
                    isSelected = idx == selectedOptionIdx,
                    selectedBorderColor = MaterialTheme.colorScheme.onBackground,
                    onClicked = {
                        onCropOptionClicked(idx, it)
                        if (it.aspectRatioX != it.aspectRatioY) {
                            verticalHorizontalClickable = true
                        } else {
                            verticalHorizontalClickable = false
                        }
                    }
                )
            }
        }
    }


}

@Composable
fun CropOptionView(
    modifier: Modifier = Modifier,
    cropOption: CropOption,
    isSelected: Boolean,
    selectedBorderWidth: Dp = 1.dp,
    selectedBorderColor: Color = MaterialTheme.colorScheme.onBackground,
    clipShape: Shape = RoundedCornerShape(4.dp),
    onClicked: (CropOption) -> Unit,
    borderColor: Color = if (isSelected) selectedBorderColor else Color.Transparent
) {


    Column(
        modifier = modifier
            .wrapContentSize()
            .clip(clipShape)
            .background(color = borderColor)
            .padding(selectedBorderWidth)
            .clip(clipShape)
            .background(MaterialTheme.colorScheme.background)
            //.padding(4.dp)
            .clickable { onClicked(cropOption) },
        horizontalAlignment = Alignment.CenterHorizontally

    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(top = 4.dp, bottom = 4.dp)
                .height(20.dp)
        ) {
            Text(
                modifier = Modifier,
                text = cropOption.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Preview
@Composable
fun show() {
    CropperXBottomToolBar(
        modifier = Modifier,
        toolbarHeight = SizeUtil.TOOLBAR_HEIGHT_X_LARGE,
        selectedOptionIdx = 0,
        onCropOptionClicked = { a, b -> },
        onCropVerticalHorizontalClicked = {}
    )
}