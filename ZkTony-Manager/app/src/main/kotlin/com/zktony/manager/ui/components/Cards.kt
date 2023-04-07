package com.zktony.manager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zktony.proto.*
import com.zktony.www.common.extension.currentTime
import java.util.*


// 功能卡片
@Composable
fun FeatureCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    onClick: () -> Unit = {},
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .clickable { onClick() }
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Blue.copy(alpha = 0.2f),
                        Color.Blue.copy(alpha = 0.1f),
                        Color.Cyan.copy(alpha = 0.2f),
                    )
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier
                    .size(48.dp)
                    .padding(top = 8.dp),
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Icon卡片
@Composable
fun IconCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit = {},
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .clickable { onClick() }
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Blue.copy(alpha = 0.2f),
                        Color.Blue.copy(alpha = 0.1f),
                        Color.Cyan.copy(alpha = 0.2f),
                    )
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier
                    .size(64.dp)
                    .padding(top = 8.dp),
                imageVector = icon,
                contentDescription = null,
                tint = color
            )
        }
    }
}

// 订单展示卡片
@Composable
fun OrderCard(
    modifier: Modifier = Modifier,
    order: Order,
    onClick: () -> Unit = {},
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .clickable { onClick() }
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Blue.copy(alpha = 0.2f),
                        Color.Blue.copy(alpha = 0.1f),
                        Color.Cyan.copy(alpha = 0.2f),
                    )
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = order.instrumentNumber,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = order.softwareId,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = order.createTime,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// 客户展示卡片
@Composable
fun CustomerCard(
    modifier: Modifier = Modifier,
    customer: Customer,
    onClick: () -> Unit = {},
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .clickable { onClick() }
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Blue.copy(alpha = 0.2f),
                        Color.Blue.copy(alpha = 0.1f),
                        Color.Cyan.copy(alpha = 0.2f),
                    )
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = customer.name,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = customer.phone,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = customer.address,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// 仪器展示卡片
@Composable
fun InstrumentCard(
    modifier: Modifier = Modifier,
    instrument: Instrument,
    onClick: () -> Unit = {},
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .clickable { onClick() }
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Blue.copy(alpha = 0.2f),
                        Color.Blue.copy(alpha = 0.1f),
                        Color.Cyan.copy(alpha = 0.2f),
                    )
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = instrument.name,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = instrument.model,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// 软件卡片
@Composable
fun SoftwareCard(
    modifier: Modifier = Modifier,
    software: Software,
    onClick: () -> Unit = {},
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .clickable { onClick() }
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Blue.copy(alpha = 0.2f),
                        Color.Blue.copy(alpha = 0.1f),
                        Color.Cyan.copy(alpha = 0.2f),
                    )
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = software.id,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = software.`package`,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = software.createTime,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


// region TextCard
@Composable
fun TextCard(
    modifier: Modifier = Modifier,
    textList: List<Pair<String, String>>,
    onClick: () -> Unit = {},
) {

    Card(
        modifier = modifier
            .fillMaxSize()
            .clickable { onClick() }
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Blue.copy(alpha = 0.2f),
                        Color.Blue.copy(alpha = 0.1f),
                        Color.Cyan.copy(alpha = 0.2f),
                    )
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            textList.forEachIndexed { index, map ->
                Row {
                    Text(
                        modifier = Modifier.width(64.dp),
                        text = map.first,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        text = ":  ${map.second}",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                if (index != textList.size - 1) {
                    Divider(modifier = Modifier.padding(vertical = 1.dp))
                }
            }
        }
    }
}
// endregion

// region AttachmentCard
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentCard(
    modifier: Modifier = Modifier,
    attachment: String,
    value: String,
    onValueChange: (String) -> Unit = {},
    onClick: () -> Unit = {},
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        onClick = { onClick() }
    ) {
        Box(
            modifier = modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Blue.copy(alpha = 0.2f),
                            Color.Blue.copy(alpha = 0.1f),
                            Color.Cyan.copy(alpha = 0.2f),
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "设备附件", style = MaterialTheme.typography.labelMedium)
                Divider(
                    modifier = Modifier.padding(vertical = 1.dp),
                    color = Color.Gray.copy(alpha = 0.2f)
                )
                val list = attachment.split("|").filter { it.isNotEmpty() || it.isNotBlank() }
                var index = 1
                list.forEach {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "$index .", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = it, style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = if (value.contains(it)) Icons.Outlined.RadioButtonChecked else Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable {
                                    if (value.contains(it)) {
                                        onValueChange(value.replace("$it|", ""))
                                    } else {
                                        onValueChange("${value + it}|")
                                    }
                                }
                        )
                    }
                    Divider(thickness = 1.dp, color = Color.LightGray)
                    index++
                }
            }
        }
    }
}
// endregion

// region preview

@Preview
@Composable
fun FeatureCardPreview() {
    FeatureCard(
        title = "title",
        icon = Icons.Default.Android
    )
}

@Preview
@Composable
fun OrderCardPreview() {
    OrderCard(
        order = order {
            id = UUID.randomUUID().toString()
            instrumentNumber = "test"
            softwareId = "test"
            createTime = currentTime()
        },
        onClick = { }
    )
}

@Preview
@Composable
fun TextCardPreview() {
    TextCard(
        textList = listOf(
            "title" to "subtitle",
            "title" to "subtitle",
            "title" to "subtitle",
        )
    )
}

@Preview
@Composable
fun AttachmentCardPreview() {
    AttachmentCard(
        attachment = "att1 att2 att3",
        value = "att1 att2 att3",
        onClick = { },
        onValueChange = { }
    )
}
// endregion