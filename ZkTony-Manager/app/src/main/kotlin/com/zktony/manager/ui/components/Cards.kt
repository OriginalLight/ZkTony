package com.zktony.manager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material.icons.outlined.ArrowForwardIos
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
import androidx.room.Update
import com.zktony.manager.data.remote.model.Product

// region FunctionCard
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FunctionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit = {},
    shape: RoundedCornerShape = RoundedCornerShape(0.dp),
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        onClick = { onClick() },
    ) {

        Box(
            modifier = Modifier.background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Blue.copy(alpha = 0.2f),
                        Color.Blue.copy(alpha = 0.1f),
                        Color.Cyan.copy(alpha = 0.2f),
                    )
                )
            )
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Unspecified
                )
                Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = Icons.Outlined.ArrowForwardIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
// endregion

// region TextCard
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextCard(
    modifier: Modifier = Modifier,
    textList: List<Pair<String, String>>,
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
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Blue.copy(alpha = 0.1f),
                            Color.Cyan.copy(alpha = 0.1f),
                            Color.Blue.copy(alpha = 0.1f),
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Blue.copy(alpha = 0.1f),
                            Color.Cyan.copy(alpha = 0.1f),
                            Color.Blue.copy(alpha = 0.1f),
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
                val list = attachment.split(" ").filter { it.isNotEmpty() }
                list.forEachIndexed() { index, it ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "${index + 1}.", style = MaterialTheme.typography.labelMedium)
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
                                        onValueChange(value.replace("$it ", ""))
                                    } else {
                                        onValueChange("${value + it} ")
                                    }
                                }
                        )
                    }
                    Divider(
                        modifier = Modifier.padding(vertical = 1.dp),
                        color = Color.Gray.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}
// endregion

// region ProductCard
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(
    modifier: Modifier = Modifier,
    product: Product,
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
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Blue.copy(alpha = 0.1f),
                            Color.Cyan.copy(alpha = 0.1f),
                            Color.Blue.copy(alpha = 0.1f),
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                Text(text = "编号: ${product.id}", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = "快递: ${product.express_number}",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "时间: ${product.create_time.replace("T", " ")}",
                    style = MaterialTheme.typography.labelMedium
                )
                if (product.create_by.isNotEmpty()) {
                    Text(
                        text = "创建人: ${product.create_by}",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}


// endregion

// region UpdateCard
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    progress: Int,
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
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.Default.Upgrade,
                    contentDescription = null,
                    tint = Color.Unspecified
                )
                Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (progress > 0) {
                    Text(
                        color = MaterialTheme.colorScheme.primary,
                        text = "$progress %"
                    )
                }
            }
        }
    }
}
// endregion

// region preview
@Preview
@Composable
fun FunctionCardPreview() {
    FunctionCard(
        title = "title",
        subtitle = "subtitle",
        icon = Icons.Default.Android
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

@Preview
@Composable
fun ProductCardPreview() {
    ProductCard(
        product = Product(),
        onClick = { }
    )
}

@Preview
@Composable
fun UpdateCardPreview() {
    UpdateCard(
        title = "title",
        subtitle = "subtitle",
        progress = 10,
        onClick = { }
    )
}
// endregion