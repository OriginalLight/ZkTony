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
    Image(Program, null)
}

private var _Program: ImageVector? = null

val Program: ImageVector
    get() {
        if (_Program != null) {
            return _Program!!
        }
        _Program = ImageVector.Builder(
            name = "Program",
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
                moveTo(376.7f, 555.3f)
                horizontalLineTo(156.4f)
                curveToRelative(-50.8f, 0f, -92f, 41.2f, -92f, 92f)
                verticalLineToRelative(220.3f)
                curveToRelative(0f, 50.8f, 41.2f, 92f, 92f, 92f)
                horizontalLineToRelative(220.3f)
                curveToRelative(50.8f, 0f, 92f, -41.2f, 92f, -92f)
                verticalLineTo(647.3f)
                curveToRelative(0f, -50.8f, -41.2f, -92f, -92f, -92f)
                close()
                moveToRelative(44f, 312.3f)
                curveToRelative(0f, 24.3f, -19.7f, 44f, -44f, 44f)
                horizontalLineTo(156.4f)
                curveToRelative(-24.3f, 0f, -44f, -19.7f, -44f, -44f)
                verticalLineTo(647.3f)
                curveToRelative(0f, -24.3f, 19.7f, -44f, 44f, -44f)
                horizontalLineToRelative(220.3f)
                curveToRelative(24.3f, 0f, 44f, 19.7f, 44f, 44f)
                verticalLineToRelative(220.3f)
                close()
                moveToRelative(500.6f, -280.3f)
                curveToRelative(-22.4f, -20.5f, -52.7f, -32f, -85.2f, -32f)
                horizontalLineTo(674.8f)
                curveToRelative(-67.1f, 0f, -121.5f, 54.4f, -121.6f, 121.6f)
                verticalLineToRelative(161.2f)
                curveToRelative(0f, 67.1f, 54.4f, 121.5f, 121.6f, 121.6f)
                horizontalLineTo(836f)
                curveToRelative(32.1f, 0f, 62.2f, -10.4f, 84.8f, -30.1f)
                curveToRelative(17.2f, -14.9f, 29.5f, -35.5f, 34.4f, -59.1f)
                curveToRelative(1.5f, -3.2f, 2.3f, -6.7f, 2.3f, -10.3f)
                verticalLineToRelative(-6.6f)
                verticalLineToRelative(-6.6f)
                curveToRelative(0f, -6.3f, -2.6f, -12.5f, -7f, -17f)
                curveToRelative(-4.5f, -4.5f, -10.7f, -7f, -17f, -7f)
                curveToRelative(-6.3f, 0f, -12.5f, 2.6f, -17f, 7f)
                curveToRelative(-4.5f, 4.5f, -7f, 10.7f, -7f, 17f)
                curveToRelative(-0.2f, 20.4f, -7.7f, 35.2f, -20.4f, 46.5f)
                curveToRelative(-12.7f, 11.2f, -31.4f, 18.2f, -53.2f, 18.2f)
                horizontalLineTo(674.8f)
                curveToRelative(-40.6f, -0.1f, -73.5f, -32.9f, -73.6f, -73.6f)
                verticalLineTo(676.9f)
                curveToRelative(0.1f, -40.6f, 32.9f, -73.5f, 73.6f, -73.6f)
                horizontalLineTo(836f)
                curveToRelative(42.9f, 0.7f, 73.3f, 27.9f, 73.6f, 67.7f)
                verticalLineToRelative(11.6f)
                curveToRelative(0f, 13.3f, 10.7f, 24f, 24f, 24f)
                reflectiveCurveToRelative(24f, -10.7f, 24f, -24f)
                verticalLineTo(671f)
                curveToRelative(0.1f, -33.5f, -14f, -63.4f, -36.3f, -83.7f)
                close()
                moveTo(866.6f, 64.4f)
                horizontalLineTo(646.3f)
                curveToRelative(-50.8f, 0f, -92f, 41.2f, -92f, 92f)
                verticalLineToRelative(220.3f)
                curveToRelative(0f, 50.8f, 41.2f, 92f, 92f, 92f)
                horizontalLineToRelative(220.3f)
                curveToRelative(50.8f, 0f, 92f, -41.2f, 92f, -92f)
                verticalLineTo(156.4f)
                curveToRelative(0f, -50.8f, -41.2f, -92f, -92f, -92f)
                close()
                moveToRelative(44f, 312.3f)
                curveToRelative(0f, 24.3f, -19.7f, 44f, -44f, 44f)
                horizontalLineTo(646.3f)
                curveToRelative(-24.3f, 0f, -44f, -19.7f, -44f, -44f)
                verticalLineTo(156.4f)
                curveToRelative(0f, -24.3f, 19.7f, -44f, 44f, -44f)
                horizontalLineToRelative(220.3f)
                curveToRelative(24.3f, 0f, 44f, 19.7f, 44f, 44f)
                verticalLineToRelative(220.3f)
                close()
                moveToRelative(23f, 364.6f)
                curveToRelative(-6.3f, 0f, -12.5f, 2.6f, -17f, 7f)
                curveToRelative(-4.5f, 4.5f, -7f, 10.7f, -7f, 17f)
                verticalLineTo(767.6f)
                curveToRelative(0f, 6.3f, 2.6f, 12.5f, 7f, 17f)
                curveToRelative(4.5f, 4.5f, 10.7f, 7f, 17f, 7f)
                curveToRelative(6.3f, 0f, 12.5f, -2.6f, 17f, -7f)
                curveToRelative(4.5f, -4.5f, 7f, -10.7f, 7f, -17f)
                verticalLineToRelative(-2.3f)
                curveToRelative(0f, -6.3f, -2.6f, -12.5f, -7f, -17f)
                curveToRelative(-4.5f, -4.4f, -10.7f, -7f, -17f, -7f)
                close()
                moveTo(376.7f, 64.4f)
                horizontalLineTo(156.4f)
                curveToRelative(-50.8f, 0f, -92f, 41.2f, -92f, 92f)
                verticalLineToRelative(220.3f)
                curveToRelative(0f, 50.8f, 41.2f, 92f, 92f, 92f)
                horizontalLineToRelative(220.3f)
                curveToRelative(50.8f, 0f, 92f, -41.2f, 92f, -92f)
                verticalLineTo(156.4f)
                curveToRelative(0f, -50.8f, -41.2f, -92f, -92f, -92f)
                close()
                moveToRelative(44f, 312.3f)
                curveToRelative(0f, 24.3f, -19.7f, 44f, -44f, 44f)
                horizontalLineTo(156.4f)
                curveToRelative(-24.3f, 0f, -44f, -19.7f, -44f, -44f)
                verticalLineTo(156.4f)
                curveToRelative(0f, -24.3f, 19.7f, -44f, 44f, -44f)
                horizontalLineToRelative(220.3f)
                curveToRelative(24.3f, 0f, 44f, 19.7f, 44f, 44f)
                verticalLineToRelative(220.3f)
                close()
            }
        }.build()
        return _Program!!
    }

