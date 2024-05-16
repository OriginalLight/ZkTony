package com.zktony.android.ui.components

import android.graphics.Color.rgb
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zktony.android.R
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.ui.SettingIntent
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.navigation.TOP_LEVEL_DESTINATIONS
import com.zktony.android.ui.navigation.TopLevelDestination
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.PageType
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun test() {




}


@Composable
fun LineChart(dataPoints: List<Float>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val path = Path()

        // 计算曲线图的坐标
        val xStep = size.width / (dataPoints.size - 1)
        val yMax = dataPoints.maxOrNull() ?: 1f
        val yStep = size.height / yMax

        dataPoints.forEachIndexed { index, y ->
            val x = index * xStep
            val yCoord = size.height - y * yStep
            val point = Offset(x, yCoord)

            if (index == 0) {
                path.moveTo(point.x, point.y)
            } else {
                path.lineTo(point.x, point.y)
            }
        }

        // 绘制曲线图
        drawPath(path, color = Color.Blue, style = Stroke(width = 4.dp.toPx()))
    }
}

@Preview
@Composable
fun LineChartPreview() {
    val dataPoints = listOf(10f, 20f, 30f, 25f, 15f, 35f, 40f)
    LineChart(dataPoints = dataPoints)
}


//@Preview
//@Composable
//fun testTar() {
//    Row(
//        modifier = Modifier
//            .height(101.dp)
//            .width(572.5.dp)
//            .background(Color.White),
//        //垂直对齐
//        verticalAlignment = Alignment.CenterVertically,
//    ) {
//        Icon(
//            modifier = Modifier
//                .size(30.dp)
//                .clickable {
////                            navigation()
//                },
//            painter = painterResource(id = R.mipmap.greenarrow),
//            contentDescription = stringResource(id = TOP_LEVEL_DESTINATIONS[4].iconTextId)
//        )
//
//        Text(modifier = Modifier.padding(start = 200.dp),text = "调试模式", fontSize = 20.sp, color = Color(rgb(112, 112, 112)))
//    }
//}


@Composable
fun shengyin() {

    var selectRudio = rememberDataSaverState(key = "selectRudio", default = 1)
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .background(
                color = Color(rgb(238, 238, 238)),
                shape = CircleShape
            )
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AUDIO_DESTINATION.forEach { destination ->
            ElevatedButton(
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectRudio.value == destination.id) Color(
                        rgb(
                            0,
                            105,
                            52
                        )
                    ) else Color(rgb(238, 238, 238)),
                ),
                onClick = {
                    selectRudio.value = destination.id

                },
            ) {
                Text(
                    text = destination.name,
                    color = if (selectRudio.value == destination.id) Color.White else Color.Black
                )
            }
        }
    }

}

val AUDIO_DESTINATION = listOf(
    AudioDestination(
        id = 1,
        name = "蜂鸣"
    ),
    AudioDestination(
        id = 2,
        name = "语音"
    ),
    AudioDestination(
        id = 3,
        name = "静音"
    )
)

data class AudioDestination(
    val id: Int,
    val name: String
)