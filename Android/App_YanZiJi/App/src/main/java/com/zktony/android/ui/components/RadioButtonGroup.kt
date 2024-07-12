package com.zktony.android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun RadioButtonGroup(
    modifier: Modifier = Modifier,
    selected: Int,
    options: List<String>,
    onSelectedChange: (Int) -> Unit
) {
    Row(
        modifier = modifier.selectableGroup(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEachIndexed { index, text ->
            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onSelectedChange(index) }
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = index == selected,
                    onClick = null
                )

                Text(text = text, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun VerticalRadioButtonGroup(
    modifier: Modifier = Modifier,
    selected: Int,
    options: List<String>,
    onSelectedChange: (Int) -> Unit
) {
    Column(
        modifier = modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEachIndexed { index, text ->
            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onSelectedChange(index) }
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = index == selected,
                    onClick = null
                )

                Text(text = text, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Preview
@Composable
fun RadioButtonGroupPreview() {
    RadioButtonGroup(
        selected = 0,
        options = listOf("Option 1", "Option 2", "Option 3"),
        onSelectedChange = { }
    )
}

@Preview
@Composable
fun VerticalRadioButtonGroupPreview() {
    VerticalRadioButtonGroup(
        selected = 0,
        options = listOf("Option 1", "Option 2", "Option 3"),
        onSelectedChange = { }
    )
}