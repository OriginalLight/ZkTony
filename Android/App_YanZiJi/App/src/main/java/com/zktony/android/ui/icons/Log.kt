package com.zktony.android.ui.icons

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Preview
@Composable
private fun VectorPreview() {
    Image(Log, null)
}

private var _Log: ImageVector? = null

val Log: ImageVector
    get() {
        if (_Log != null) {
            return _Log!!
        }
        _Log = ImageVector.Builder(
            name = "History",
            defaultWidth = 256.dp,
            defaultHeight = 256.dp,
            viewportWidth = 1024f,
            viewportHeight = 1024f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF4E4D4D)),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(269.844659f, 81.4308f)
                horizontalLineToRelative(44.821057f)
                verticalLineToRelative(166.626082f)
                horizontalLineToRelative(-44.821057f)
                close()
                moveTo(677.140966f, 491.719232f)
                curveToRelative(52.3354f, 0f, 102.0923f, 19.9378f, 140.1056f, 56.1388f)
                curveToRelative(38.1265f, 36.3105f, 60.4616f, 85.2841f, 62.8918f, 137.9005f)
                curveToRelative(2.5056f, 54.2767f, -16.2742f, 106.28f, -52.8815f, 146.4317f)
                curveToRelative(-36.6073f, 40.1516f, -86.6597f, 63.6435f, -140.9364f, 66.1503f)
                curveToRelative(-3.1807f, 0.1472f, -6.4014f, 0.2214f, -9.576f, 0.2214f)
                curveToRelative(-52.3415f, 0f, -102.102f, -19.9366f, -140.1142f, -56.1364f)
                curveToRelative(-38.1265f, -36.3093f, -60.4616f, -85.2841f, -62.8918f, -137.9029f)
                curveToRelative(-2.5056f, -54.2767f, 16.2742f, -106.28f, 52.8815f, -146.4317f)
                curveToRelative(36.6073f, -40.1516f, 86.6597f, -63.6435f, 140.9364f, -66.1491f)
                arcToRelative(
                    208.122961f,
                    208.122961f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    9.576016f,
                    -0.221369f
                )
                horizontalLineToRelative(0.008514f)
                moveToRelative(-0.00973f, -44.822274f)
                curveToRelative(-3.8594f, 0f, -7.7467f, 0.0888f, -11.6425f, 0.2688f)
                curveToRelative(-136.9517f, 6.3236f, -242.8474f, 122.4703f, -236.525f, 259.4221f)
                curveToRelative(6.1436f, 133.0559f, 115.9424f, 236.7938f, 247.7796f, 236.7938f)
                curveToRelative(3.8594f, 0f, 7.7479f, -0.0888f, 11.6425f, -0.2688f)
                curveToRelative(136.9517f, -6.3224f, 242.8474f, -122.4703f, 236.525f, -259.4221f)
                curveToRelative(
                    -6.1436f,
                    -133.0571f,
                    -115.9424f,
                    -236.7987f,
                    -247.7796f,
                    -236.7938f
                )
                close()
            }
            path(
                fill = SolidColor(Color(0xFF4E4D4D)),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(490.264524f, 891.110734f)
                arcToRelative(
                    272.361206f,
                    272.361206f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -32.682275f,
                    -37.369937f
                )
                horizontalLineTo(180.453104f)
                curveToRelative(-20.912f, 0f, -37.927f, -17.0138f, -37.927f, -37.9258f)
                verticalLineToRelative(-590.263526f)
                curveToRelative(0f, -20.912f, 17.0138f, -37.927f, 37.927f, -37.927f)
                horizontalLineTo(732.799354f)
                curveToRelative(20.912f, 0f, 37.9258f, 17.0138f, 37.9258f, 37.927f)
                verticalLineTo(441.15597f)
                arcToRelative(
                    268.605238f,
                    268.605238f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    44.821057f,
                    21.463023f
                )
                verticalLineTo(225.551481f)
                curveToRelative(0f, -45.7004f, -37.0476f, -82.7468f, -82.7468f, -82.7468f)
                horizontalLineTo(180.453104f)
                curveToRelative(-45.7004f, 0f, -82.7468f, 37.0476f, -82.7468f, 82.7468f)
                verticalLineToRelative(590.263526f)
                curveToRelative(0f, 45.7004f, 37.0476f, 82.7468f, 82.7468f, 82.7468f)
                horizontalLineToRelative(317.980164f)
                arcToRelative(
                    273.587248f,
                    273.587248f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -8.168744f,
                    -7.451121f
                )
                close()
            }
            path(
                fill = SolidColor(Color(0xFF4E4D4D)),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(770.725145f, 489.61623f)
                arcToRelative(
                    225.243754f,
                    225.243754f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    44.821057f,
                    27.231985f
                )
                verticalLineToRelative(-0.21407f)
                arcToRelative(
                    225.182938f,
                    225.182938f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -44.821057f,
                    -27.114003f
                )
                verticalLineToRelative(0.096088f)
                close()
                moveTo(812.590566f, 778.530212f)
                horizontalLineTo(646.820768f)
                verticalLineTo(576.105667f)
                horizontalLineToRelative(44.821057f)
                verticalLineToRelative(157.604704f)
                horizontalLineToRelative(120.948741f)
                close()
                moveTo(209.55091f, 380.121489f)
                horizontalLineToRelative(498.255687f)
                verticalLineToRelative(44.821057f)
                horizontalLineTo(209.55091f)
                close()
                moveTo(600.682445f, 81.4308f)
                horizontalLineToRelative(44.821058f)
                verticalLineToRelative(166.626082f)
                horizontalLineToRelative(-44.821058f)
                close()
                moveTo(406.842623f, 712.17437f)
                horizontalLineTo(209.55091f)
                verticalLineToRelative(44.821057f)
                horizontalLineToRelative(203.864657f)
                arcToRelative(
                    272.351476f,
                    272.351476f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -6.572944f,
                    -44.821057f
                )
                close()
                moveTo(450.941192f, 546.147929f)
                horizontalLineTo(209.55091f)
                verticalLineToRelative(44.821057f)
                horizontalLineToRelative(217.435038f)
                arcToRelative(
                    268.707408f,
                    268.707408f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    23.955244f,
                    -44.821057f
                )
                close()
            }
        }.build()
        return _Log!!
    }



