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
    Image(Logout, null)
}

private var _Logout: ImageVector? = null

val Logout: ImageVector
    get() {
        if (_Logout != null) {
            return _Logout!!
        }
        _Logout = ImageVector.Builder(
            name = "Logout",
            defaultWidth = 256.dp,
            defaultHeight = 256.dp,
            viewportWidth = 1024f,
            viewportHeight = 1024f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(657.408f, 960f)
                horizontalLineToRelative(-460.8f)
                curveToRelative(-50.688f, 0f, -92.16f, -41.472f, -92.16f, -92.16f)
                verticalLineToRelative(-711.68f)
                curveToRelative(0f, -50.688f, 41.472f, -92.16f, 92.16f, -92.16f)
                horizontalLineToRelative(460.8f)
                curveToRelative(50.688f, 0f, 92.16f, 41.472f, 92.16f, 92.16f)
                verticalLineTo(271.36f)
                horizontalLineToRelative(-61.44f)
                verticalLineTo(156.16f)
                curveToRelative(0f, -16.896f, -13.824f, -30.72f, -30.72f, -30.72f)
                horizontalLineToRelative(-460.8f)
                curveToRelative(-16.896f, 0f, -30.72f, 13.824f, -30.72f, 30.72f)
                verticalLineToRelative(711.68f)
                curveToRelative(0f, 16.896f, 13.824f, 30.72f, 30.72f, 30.72f)
                horizontalLineToRelative(460.8f)
                curveToRelative(16.896f, 0f, 30.72f, -13.824f, 30.72f, -30.72f)
                verticalLineToRelative(-101.376f)
                horizontalLineToRelative(61.44f)
                verticalLineToRelative(101.376f)
                curveToRelative(0f, 50.688f, -41.472f, 92.16f, -92.16f, 92.16f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(888.832f, 542.72f)
                horizontalLineToRelative(-481.28f)
                curveToRelative(-16.896f, 0f, -30.72f, -13.824f, -30.72f, -30.72f)
                reflectiveCurveToRelative(13.824f, -30.72f, 30.72f, -30.72f)
                horizontalLineToRelative(481.28f)
                curveToRelative(16.896f, 0f, 30.72f, 13.824f, 30.72f, 30.72f)
                reflectiveCurveToRelative(-13.824f, 30.72f, -30.72f, 30.72f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(867.328f, 534.016f)
                lineToRelative(-137.728f, -137.728f)
                curveToRelative(-11.776f, -11.776f, -11.776f, -31.744f, 0f, -43.52f)
                reflectiveCurveToRelative(31.744f, -11.776f, 43.52f, 0f)
                lineToRelative(137.728f, 137.728f)
                curveToRelative(11.776f, 11.776f, 11.776f, 31.744f, 0f, 43.52f)
                curveToRelative(-12.288f, 11.776f, -31.744f, 11.776f, -43.52f, 0f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(867.328f, 490.496f)
                lineToRelative(-137.728f, 137.728f)
                curveToRelative(-11.776f, 11.776f, -11.776f, 31.744f, 0f, 43.52f)
                reflectiveCurveToRelative(31.744f, 11.776f, 43.52f, 0f)
                lineToRelative(137.728f, -137.728f)
                curveToRelative(11.776f, -11.776f, 11.776f, -31.744f, 0f, -43.52f)
                curveToRelative(-12.288f, -11.776f, -31.744f, -11.776f, -43.52f, 0f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(718.848f, 271.36f)
                moveToRelative(-30.72f, 0f)
                arcToRelative(
                    30.72f,
                    30.72f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    61.44f,
                    0f
                )
                arcToRelative(
                    30.72f,
                    30.72f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    -61.44f,
                    0f
                )
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(718.848f, 767.488f)
                moveToRelative(-30.72f, 0f)
                arcToRelative(
                    30.72f,
                    30.72f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    61.44f,
                    0f
                )
                arcToRelative(
                    30.72f,
                    30.72f,
                    0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    -61.44f,
                    0f
                )
                close()
            }
        }.build()
        return _Logout!!
    }

