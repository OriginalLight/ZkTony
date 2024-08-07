package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.of
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import com.patrykandpatrick.vico.compose.common.rememberLegendItem
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.shape.Shape
import com.zktony.android.ui.components.BaseTopBar
import com.zktony.android.ui.components.IconLoading
import com.zktony.android.ui.components.ListEmptyView
import com.zktony.android.ui.components.rememberMarker
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.getAttributes
import com.zktony.android.ui.utils.zktyChartLinearColors
import com.zktony.android.ui.utils.zktyHorizontalBrush
import com.zktony.android.ui.viewmodel.LogDetailViewModel
import com.zktony.android.utils.extra.dateFormat
import com.zktony.room.defaultLog
import com.zktony.room.entities.Log
import com.zktony.room.entities.LogSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@Composable
fun LogDetailView(viewModel: LogDetailViewModel = hiltViewModel()) {
    val navigationActions = LocalNavigationActions.current

    BackHandler {
        navigationActions.navigateUp()
    }

    val navObj by viewModel.navObj.collectAsStateWithLifecycle()
    val entities by viewModel.entities.collectAsStateWithLifecycle()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 顶部导航栏
        LogDetailTopBar(navigationActions = navigationActions)
        // 内容
        LogDetailContent(
            log = navObj ?: defaultLog(),
            entities = entities
        )
    }
}

// 顶部导航栏
@Composable
fun LogDetailTopBar(
    modifier: Modifier = Modifier,
    navigationActions: NavigationActions
) {
    var loadingExport by remember { mutableStateOf(false) }

    BaseTopBar(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .clickable { navigationActions.navigateUp() }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.Reply,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = "实验详情",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        // 导出
        Button(
            enabled = !loadingExport,
            onClick = {}
        ) {
            IconLoading(loading = loadingExport) {
                Icon(imageVector = Icons.Default.ImportExport, contentDescription = "Export")
            }
            Text(text = "导出", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun LogDetailContent(
    modifier: Modifier = Modifier,
    log: Log,
    entities: List<LogSnapshot>
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(
                modifier = Modifier
                    .width(350.dp)
                    .fillMaxHeight()
                    .drawWithCache {
                        onDrawBehind {
                            drawRoundRect(
                                brush = zktyHorizontalBrush,
                                size = size.copy(height = 96.dp.toPx()),
                                cornerRadius = CornerRadius(16.dp.toPx())
                            )
                        }
                    }
            ) {
                Row(
                    modifier = modifier
                        .height(48.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = when (log.status) {
                            0 -> "完成"
                            1 -> "中止"
                            2 -> "出错"
                            else -> "未知"
                        },
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "通道 ${log.channel + 1}",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    log.getAttributes().forEach { it ->
                        it?.let {
                            Row {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = it.first,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.End
                                )
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = it.second,
                                    fontSize = 18.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Column {
                    Text(
                        text = "开始时间：${log.createTime.dateFormat("yyyy-MM-dd HH:mm:ss")}",
                        fontSize = 18.sp
                    )
                    Text(
                        text = "结束时间：${log.endTime.dateFormat("yyyy-MM-dd HH:mm:ss")}",
                        fontSize = 18.sp
                    )
                }

                if (entities.isNotEmpty()) {
                    LogSnapshotChart(
                        modifier = Modifier.fillMaxSize(),
                        entities = entities
                    )
                } else {
                    ListEmptyView()
                }
            }
        }
    }
}

@Composable
fun LogSnapshotChart(
    modifier: Modifier = Modifier,
    entities: List<LogSnapshot>
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            modelProducer.runTransaction {
                val scale = maxOf(1, (entities.size / 60f).roundToInt())
                val items = entities.filter { it.time % scale == 0 }
                lineSeries {
                    series(
                        x = items.map { it.time }.toList(),
                        y = items.map { it.current.toFloatOrNull() ?: 0f }.toList()
                    )
                    series(
                        x = items.map { it.time }.toList(),
                        y = items.map { it.voltage.toFloatOrNull() ?: 0f }.toList()
                    )
                    series(
                        x = items.map { it.time }.toList(),
                        y = items.map { it.power.toFloatOrNull() ?: 0f }.toList()
                    )
                }
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                LineCartesianLayer.LineProvider.series(
                    zktyChartLinearColors.map { color ->
                        rememberLine(
                            fill = remember { LineCartesianLayer.LineFill.single(fill(color)) },
                            areaFill = null,
                        )
                    }
                )
            ),
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis(

            ),
            marker = rememberMarker(),
            legend = rememberHorizontalLegend(
                items = zktyChartLinearColors.mapIndexed { index, chartColor ->
                    rememberLegendItem(
                        icon = rememberShapeComponent(chartColor, Shape.Pill),
                        labelComponent = rememberTextComponent(Color.Black),
                        label = when (index) {
                            0 -> "电流"
                            1 -> "电压"
                            2 -> "功率"
                            else -> ""
                        },
                    )
                },
                iconSize = 12.dp,
                iconPadding = 4.dp,
                spacing = 16.dp,
                padding = Dimensions.of(start = 8.dp, top = 8.dp),
            )
        ),
        modelProducer = modelProducer,
        modifier = modifier,
        zoomState = rememberVicoZoomState(),
        scrollState = rememberVicoScrollState()
    )
}

