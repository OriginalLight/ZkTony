package com.zktony.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DropDownBox(
    modifier: Modifier = Modifier,
    selected: Int,
    options: List<String>,
    onSelectedChange: (Int) -> Unit
) {

    var option by remember { mutableStateOf(options[selected]) }
    var expended by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, CircleShape)
            .clip(CircleShape)
            .clickable { expended = !expended }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = option, style = MaterialTheme.typography.bodyLarge
            )

            Icon(
                imageVector = if (expended) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Expand"
            )
        }

        DropdownMenu(
            expanded = expended,
            onDismissRequest = { expended = false }
        ) {
            options.forEachIndexed { index, text ->
                DropdownMenuItem(
                    text = { Text(text, style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        option = text
                        onSelectedChange(index)
                        expended = false
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun DropDownBoxPreview() {
    Surface(color = MaterialTheme.colorScheme.onSurface) {
        DropDownBox(
            modifier = Modifier
                .width(200.dp)
                .height(48.dp),
            selected = 0,
            options = listOf("Option 1", "Option 2", "Option 3"),
            onSelectedChange = { }
        )
    }
}