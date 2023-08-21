package com.zktony.android.ui.components.timeline

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp

data class CircleParameters(
    val radius: Dp,
    val backgroundColor: Color,
    val stroke: StrokeParameters? = null,
    val icon: ImageVector? = null,
    val iconColorFilter: ColorFilter,
)

