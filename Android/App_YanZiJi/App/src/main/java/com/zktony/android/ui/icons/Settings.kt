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
    Image(Settings, null)
}

private var _Settings: ImageVector? = null

val Settings: ImageVector
    get() {
        if (_Settings != null) {
            return _Settings!!
        }
        _Settings = ImageVector.Builder(
            name = "Settings",
            defaultWidth = 256.dp,
            defaultHeight = 256.dp,
            viewportWidth = 1024f,
            viewportHeight = 1024f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF040000)),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(512f, 325.818182f)
                curveToRelative(-115.6655f, 0f, -209.4545f, 93.7891f, -209.4545f, 209.4545f)
                curveToRelative(0f, 115.6655f, 93.7891f, 209.4545f, 209.4545f, 209.4545f)
                reflectiveCurveToRelative(209.454545f, -93.789091f, 209.454545f, -209.454545f)
                curveTo(721.4545f, 419.6073f, 627.6655f, 325.8182f, 512f, 325.8182f)
                close()
                moveTo(512f, 698.181818f)
                curveToRelative(-89.9724f, 0f, -162.9091f, -72.9367f, -162.9091f, -162.9091f)
                curveTo(349.0909f, 445.3004f, 422.0276f, 372.3636f, 512f, 372.3636f)
                reflectiveCurveToRelative(162.909091f, 72.936727f, 162.909091f, 162.909091f)
                curveTo(674.9091f, 625.2451f, 601.9724f, 698.1818f, 512f, 698.1818f)
                close()
                moveTo(907.636364f, 418.909091f)
                lineToRelative(-66.746182f, 0f)
                curveToRelative(-4.1425f, -11.5898f, -8.7971f, -22.9004f, -14.0567f, -33.9316f)
                lineToRelative(47.197091f, -47.197091f)
                curveToRelative(18.1993f, -18.1993f, 18.1993f, -47.6625f, 0f, -65.8153f)
                lineToRelative(-98.722909f, -98.722909f)
                curveToRelative(-18.1993f, -18.1993f, -47.6625f, -18.1993f, -65.8153f, 0f)
                lineToRelative(-47.429818f, 47.429818f)
                curveToRelative(-10.9382f, -5.2131f, -22.1091f, -10.1004f, -33.6524f, -14.1964f)
                lineTo(628.410182f, 139.636364f)
                curveToRelative(0f, -25.6931f, -20.8524f, -46.5455f, -46.5455f, -46.5455f)
                lineToRelative(-139.636364f, 0f)
                curveToRelative(-25.6931f, 0f, -46.5455f, 20.8524f, -46.5455f, 46.5455f)
                lineToRelative(0f, 66.839273f)
                curveTo(384.0931f, 210.5716f, 372.8756f, 215.3658f, 361.8909f, 220.6255f)
                lineTo(314.507636f, 173.242182f)
                curveToRelative(-18.1527f, -18.1993f, -47.6625f, -18.1993f, -65.8153f, 0f)
                lineTo(149.969455f, 271.965091f)
                curveToRelative(-18.1993f, 18.1993f, -18.1993f, 47.6625f, 0f, 65.8153f)
                lineToRelative(47.290182f, 47.290182f)
                curveTo(192f, 396.0553f, 187.1593f, 407.3193f, 183.0633f, 418.9091f)
                lineTo(116.363636f, 418.909091f)
                curveToRelative(-25.6931f, 0f, -46.5455f, 20.8524f, -46.5455f, 46.5455f)
                lineToRelative(0f, 139.636364f)
                curveToRelative(0f, 25.6931f, 20.8524f, 46.5455f, 46.5455f, 46.5455f)
                lineToRelative(66.746182f, 0f)
                curveToRelative(4.1425f, 11.5898f, 8.7971f, 22.9004f, 14.0567f, 33.8851f)
                lineToRelative(-47.197091f, 47.197091f)
                curveToRelative(-18.1993f, 18.1527f, -18.1993f, 47.6625f, 0f, 65.8153f)
                lineToRelative(98.722909f, 98.722909f)
                curveToRelative(18.1993f, 18.1993f, 47.6625f, 18.1993f, 65.8153f, 0f)
                lineToRelative(47.429818f, -47.429818f)
                curveToRelative(10.9382f, 5.2131f, 22.1091f, 10.1004f, 33.6524f, 14.1964f)
                lineTo(395.589818f, 930.909091f)
                curveToRelative(0f, 25.6931f, 20.8524f, 46.5455f, 46.5455f, 46.5455f)
                lineToRelative(139.636364f, 0f)
                curveToRelative(25.6931f, 0f, 46.5455f, -20.8524f, 46.5455f, -46.5455f)
                lineToRelative(0f, -66.746182f)
                curveToRelative(11.5898f, -4.1425f, 22.9004f, -8.7971f, 33.8851f, -14.0567f)
                lineToRelative(47.197091f, 47.197091f)
                curveToRelative(18.1993f, 18.1993f, 47.6625f, 18.1993f, 65.8153f, 0f)
                lineToRelative(98.722909f, -98.722909f)
                curveToRelative(18.1993f, -18.1993f, 18.1993f, -47.6625f, 0f, -65.8153f)
                lineToRelative(-47.429818f, -47.429818f)
                curveToRelative(5.2131f, -10.9382f, 10.1004f, -22.1091f, 14.1964f, -33.6524f)
                lineTo(907.636364f, 651.682909f)
                curveToRelative(25.6931f, 0f, 46.5455f, -20.8524f, 46.5455f, -46.5455f)
                lineToRelative(0f, -139.636364f)
                curveTo(954.1818f, 439.7615f, 933.3295f, 418.9091f, 907.6364f, 418.9091f)
                close()
                moveTo(907.636364f, 581.818182f)
                curveToRelative(0f, 12.8465f, -10.4262f, 23.2727f, -23.2727f, 23.2727f)
                lineToRelative(-78.242909f, 0f)
                curveToRelative(-7.5404f, 31.8371f, -20.1076f, 61.6727f, -36.8175f, 88.7622f)
                lineToRelative(55.342545f, 55.389091f)
                curveToRelative(9.0764f, 9.0764f, 9.0764f, 23.8313f, 0f, 32.9076f)
                lineToRelative(-65.815273f, 65.815273f)
                curveToRelative(-9.0764f, 9.0764f, -23.8313f, 9.0764f, -32.9076f, 0f)
                lineToRelative(-55.342545f, -55.342545f)
                curveToRelative(-27.0895f, 16.7098f, -56.9251f, 29.2771f, -88.7622f, 36.8175f)
                lineTo(581.818182f, 907.636364f)
                curveToRelative(0f, 12.8465f, -10.4262f, 23.2727f, -23.2727f, 23.2727f)
                lineToRelative(-93.090909f, 0f)
                curveToRelative(-12.8465f, 0f, -23.2727f, -10.4262f, -23.2727f, -23.2727f)
                lineToRelative(0f, -78.242909f)
                curveToRelative(-31.8371f, -7.5404f, -61.6727f, -20.1076f, -88.7622f, -36.8175f)
                lineToRelative(-55.342545f, 55.342545f)
                curveToRelative(-9.0764f, 9.0764f, -23.8313f, 9.0764f, -32.9076f, 0f)
                lineToRelative(-65.815273f, -65.815273f)
                curveToRelative(-9.0764f, -9.0764f, -9.0764f, -23.8313f, 0f, -32.9076f)
                lineToRelative(55.342545f, -55.389091f)
                curveTo(237.9869f, 666.7636f, 225.4196f, 636.928f, 217.8793f, 605.0909f)
                lineTo(139.636364f, 605.090909f)
                curveToRelative(-12.8465f, 0f, -23.2727f, -10.4262f, -23.2727f, -23.2727f)
                lineToRelative(0f, -93.090909f)
                curveTo(116.3636f, 475.8807f, 126.7898f, 465.4545f, 139.6364f, 465.4545f)
                lineToRelative(78.242909f, 0f)
                curveToRelative(7.5404f, -31.8371f, 20.1076f, -61.6727f, 36.8175f, -88.7622f)
                lineTo(199.307636f, 321.349818f)
                curveToRelative(-9.0764f, -9.0764f, -9.0764f, -23.8313f, 0f, -32.9076f)
                lineToRelative(65.815273f, -65.815273f)
                curveToRelative(9.0764f, -9.0764f, 23.8313f, -9.0764f, 32.9076f, 0f)
                lineToRelative(55.342545f, 55.342545f)
                curveTo(380.5091f, 261.2596f, 410.3447f, 248.6924f, 442.1818f, 241.152f)
                lineTo(442.181818f, 162.909091f)
                curveTo(442.1818f, 150.0625f, 452.608f, 139.6364f, 465.4545f, 139.6364f)
                lineToRelative(93.090909f, 0f)
                curveToRelative(12.8465f, 0f, 23.2727f, 10.4262f, 23.2727f, 23.2727f)
                lineToRelative(0f, 78.242909f)
                curveToRelative(31.8371f, 7.5404f, 61.6727f, 20.1076f, 88.7622f, 36.8175f)
                lineToRelative(55.342545f, -55.342545f)
                curveToRelative(9.0764f, -9.0764f, 23.8313f, -9.0764f, 32.9076f, 0f)
                lineToRelative(65.815273f, 65.815273f)
                curveToRelative(9.0764f, 9.0764f, 9.0764f, 23.8313f, 0f, 32.9076f)
                lineToRelative(-55.342545f, 55.342545f)
                curveToRelative(16.7098f, 27.0895f, 29.2771f, 56.8785f, 36.8175f, 88.7622f)
                lineTo(884.363636f, 465.454545f)
                curveToRelative(12.8465f, 0f, 23.2727f, 10.4262f, 23.2727f, 23.2727f)
                lineTo(907.636364f, 581.818182f)
                close()
            }
        }.build()
        return _Settings!!
    }

