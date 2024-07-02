package com.zktony.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zktony.android.R

@Composable
fun ArgumentsInputField(
    modifier: Modifier = Modifier,
    maxLength: Int = 64,
    showClear: Boolean = true,
    value: String,
    onValueChange: (String) -> Unit
) {
    var text by remember { mutableStateOf(value) }

    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, CircleShape)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showClear && text.isNotEmpty()) {
            IconButton(onClick = { text = "" }) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear"
                )
            }
        }

        BasicTextField(
            modifier = Modifier
                .weight(1f)
                .padding(start = if (text.isNotEmpty()) 0.dp else 8.dp, end = 8.dp),
            value = text,
            onValueChange = {
                if (it.length <= maxLength) {
                    text = it
                }
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge
        )

        Button(
            onClick = { onValueChange(text) }) {
            Text(text = stringResource(id = R.string.set))
        }
    }
}


@Preview
@Composable
fun ArgumentsInputGroupPreview() {
    ArgumentsInputField(value = "Hello", onValueChange = {})
}