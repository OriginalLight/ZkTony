package com.zktony.android.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * @author 刘贺贺
 * @date 2023/8/8 13:56
 */
@Composable
fun CircleTextField(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Decimal,
    trailingIcon:  @Composable (() -> Unit)? = null,
    onValueChange: (String) -> Unit = {}

) {
    val softKeyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    TextField(
        modifier = modifier.fillMaxWidth(),
        value = TextFieldValue(value, TextRange(value.length)),
        onValueChange = {
            onValueChange(it.text)
        },
        leadingIcon = {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = title,
                fontStyle = FontStyle.Italic,
                fontSize = 20.sp,
                fontFamily = FontFamily.Serif,
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                softKeyboard?.hide()
                focusManager.clearFocus()
            }
        ),
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
        ),
        textStyle = TextStyle(
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            fontFamily = FontFamily.Monospace,
        ),
        trailingIcon = trailingIcon
    )
}

@Composable
fun SquareTextField(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Decimal,
    trailingIcon: @Composable (() -> Unit)?,
    onValueChange: (String) -> Unit = {}
) {
    val softKeyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    TextField(
        modifier = modifier.fillMaxWidth(),
        value = TextFieldValue(value, TextRange(value.length)),
        onValueChange = {
            onValueChange(it.text)
        },
        leadingIcon = {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        },
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                softKeyboard?.hide()
                focusManager.clearFocus()
            }
        ),
        shape = MaterialTheme.shapes.small,
        colors = TextFieldDefaults.colors(
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
        ),
        textStyle = MaterialTheme.typography.titleMedium
    )
}

@Preview
@Composable
fun CircleTextFieldPreview() {
    CircleTextField(
        title = "半径",
        value = "1.0"
    )
}