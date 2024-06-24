package com.zktony.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
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
                    .height(48.dp)
                    .selectable(
                        selected = index == selected,
                        onClick = { onSelectedChange(index) },
                        role = Role.RadioButton
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = index == selected,
                    onClick = null
                )

                Text(text = text)
            }
        }
    }
}