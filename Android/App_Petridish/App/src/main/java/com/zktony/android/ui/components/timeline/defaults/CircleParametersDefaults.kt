package com.zktony.android.ui.components.timeline.defaults

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zktony.android.ui.components.timeline.CircleParameters
import com.zktony.android.ui.components.timeline.StrokeParameters

object CircleParametersDefaults {

    private val defaultCircleRadius = 12.dp

    fun circleParameters(
        radius: Dp = defaultCircleRadius,
        backgroundColor: Color = Color.Cyan,
        stroke: StrokeParameters? = null,
        icon: ImageVector? = null,
        iconColorFilter: ColorFilter = ColorFilter.tint(Color.White)
    ) = CircleParameters(
        radius,
        backgroundColor,
        stroke,
        icon,
        iconColorFilter,
    )
}