package com.zktony.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * @author 刘贺贺
 * @date 2023/8/9 8:49
 */
@Composable
fun CircleTabRow(
    modifier: Modifier = Modifier,
    tabItems: List<String>,
    selected: Int,
    onClick: (Int) -> Unit = {},
) {
    TabRow(
        modifier = modifier
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clip(CircleShape),
        selectedTabIndex = selected,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        indicator = { Box {} },
        divider = { },
    ) {
        tabItems.forEachIndexed { index, s ->
            Tab(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (selected == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer),
                selected = selected == index,
                onClick = { onClick(index) }
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 12.dp),
                    text = s,
                    color = if (selected == index) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Preview
@Composable
fun CircleTabRowPreview() {
    CircleTabRow(
        tabItems = listOf("1", "2", "3", "4", "5"),
        selected = 0,
        onClick = {}
    )
}