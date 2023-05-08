package com.zktony.android.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    value: String,
    hint: String? = null,
    onTextChange: (String) -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardActions: String.() -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    textStyle: TextStyle = LocalTextStyle.current,
    hiltTextStyle: TextStyle = LocalTextStyle.current,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingIcon?.invoke()
        BasicTextField(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            value = value,
            onValueChange = onTextChange,
            cursorBrush = SolidColor(textStyle.color),
            singleLine = true,
            textStyle = textStyle,
            decorationBox = { innerTextField ->
                if (value.isEmpty() && !hint.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxHeight(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        innerTextField()
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = hint,
                            style = hiltTextStyle
                        )
                    }
                } else {
                    innerTextField()
                }
            },
            keyboardOptions = keyboardOptions,
            keyboardActions = KeyboardActions {
                keyboardActions.invoke(value)
            }
        )
        trailingIcon?.invoke()
    }
}

@Composable
@Preview(showBackground = true)
fun CustomTextFieldPreview() {
    CustomTextField(
        value = "text",
        onTextChange = {},
        hint = "Hint",
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Filled.Clear,
                contentDescription = "Search",
            )
        }
    )
}