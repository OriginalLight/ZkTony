package com.zktony.android.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * @author 刘贺贺
 * @date 2023/8/3 14:20
 */
@Composable
fun VerificationCodeItem(text: String, focused: Boolean) {
    // Determine the border color based on the focus state
    val borderColor = if (focused) {
        MaterialTheme.colorScheme.onBackground
    } else {
        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
    }

    // Display the verification code item
    Box(
        modifier = Modifier
            .border(4.dp, borderColor, RoundedCornerShape(8.dp))
            .size(64.dp, 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text, fontSize = 28.sp, textAlign = TextAlign.Center, maxLines = 1
        )
    }
}

@Composable
fun VerificationCodeField(
    digits: Int,
    horizontalMargin: Dp = 16.dp,
    inputCallback: (content: String) -> Unit = {},
    itemScope: @Composable (text: String, focused: Boolean) -> Unit,
) {
    var content by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Delay the focus request and keyboard show to avoid race conditions
    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Box {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Display each verification code item
            repeat(digits) {
                if (it != 0) {
                    // Add horizontal margin between verification code items
                    Spacer(modifier = Modifier.width(horizontalMargin))
                }
                // Determine the text to display in the verification code item
                val text = if (content.getOrNull(it) != null) "*" else ""
                // Determine whether the verification code item is focused
                val focused = it == content.length
                // Display the verification code item
                itemScope(text, focused)
            }
        }
        // Display the text field for entering the verification code
        BasicTextField(
            modifier = Modifier
                .focusRequester(focusRequester)
                .drawWithContent { }// Clear the content to avoid overlapping with the verification code items
                .matchParentSize(),
            value = content,
            onValueChange = {
                content = it
                if (it.length == digits) {
                    if (it == "123456") {
                        // Invoke the input callback when the verification code is entered
                        inputCallback(it)
                        keyboardController?.hide()
                    } else {
                        // Clear the verification code field and request focus again if the verification code is incorrect
                        content = ""
                        focusRequester.requestFocus()
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                keyboardController?.hide()
            }),
        )
    }
}