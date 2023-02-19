package com.zktony.manager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zktony.manager.data.remote.model.Software

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
        onClick = { onClick() }
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
            )
            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontFamily = FontFamily.Cursive,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
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
// endregion

// region SoftwareCard
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoftwareCard(
    modifier: Modifier = Modifier,
    software: Software,
    onClick: () -> Unit = {},
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        onClick = { }
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row {
                Text(
                    modifier = Modifier.width(48.dp),
                    text = "编号",
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    text = ":  ${software.id}",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Divider(modifier = Modifier.padding(vertical = 1.dp))
            Row {
                Text(
                    modifier = Modifier.width(48.dp),
                    text = "包名",
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    text = ":  ${software.`package`}",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Divider(modifier = Modifier.padding(vertical = 1.dp))
            Row {
                Text(
                    modifier = Modifier.width(48.dp),
                    text = "版本名",
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    text = ":  ${software.version_name}",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Divider(modifier = Modifier.padding(vertical = 1.dp))
            Row {
                Text(
                    modifier = Modifier.width(48.dp),
                    text = "版本号",
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    text = ":  ${software.version_code}",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Divider(modifier = Modifier.padding(vertical = 1.dp))
            Row {
                Text(
                    modifier = Modifier.width(48.dp),
                    text = "构建类型",
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    text = ":  ${software.build_type}",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            AnimatedVisibility(visible = software.remarks.isNotEmpty()) {
                Divider(modifier = Modifier.padding(vertical = 1.dp))
                Row {
                    Text(
                        modifier = Modifier.width(48.dp),
                        text = "备注说明",
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text(
                        text = ":  ${software.remarks}",
                        style = MaterialTheme.typography.labelSmall,
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
fun SoftwareCardPreview() {
    SoftwareCard(
        software = Software(
            id = "sdasfafewsd34",
            `package` = "com.zktony.manager",
            version_name = "1.0.0",
            version_code = 1,
            build_type = "release",
            remarks = "fuyfuyfuykfuyj"
        )
    )
}
// endregion