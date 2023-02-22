package com.zktony.manager.ui.components

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.zktony.manager.data.remote.model.Software
import com.zktony.manager.ui.QrCodeActivity

/**
 * @author: 刘贺贺
 * @date: 2023-02-22 15:34
 */

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QrCodeTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    onSoftwareChange: (Software) -> Unit,
) {

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val localFocusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var androidIdError by remember { mutableStateOf(false) }
    val qrCodeScanner =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val result = it.data?.getStringExtra("SCAN_RESULT")
                // result 是json字符串解析成software对象
                try {
                    val software = Gson().fromJson(result, Software::class.java)
                    onSoftwareChange(software)
                } catch (e: Exception) {
                    androidIdError = true
                }
            }
        }

    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Cyan.copy(alpha = 0.4f),
                        Color.Blue.copy(alpha = 0.1f),
                        Color.Blue.copy(alpha = 0.2f),
                    )
                ),
                shape = RoundedCornerShape(4.dp)
            )
            .focusRequester(focusRequester),
        isError = androidIdError,
        value = value,
        label = { Text(text = "Android ID") },
        onValueChange = { onValueChange(it) },
        leadingIcon = {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = Icons.Outlined.Key,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        },
        trailingIcon = {
            Icon(
                modifier = Modifier
                    .size(32.dp)
                    .absoluteOffset(x = (-8).dp)
                    .clickable {
                        qrCodeScanner.launch(
                            Intent(
                                context, QrCodeActivity::class.java
                            )
                        )
                    },
                imageVector = Icons.Outlined.QrCode,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.textFieldColors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            containerColor = Color.Transparent,
        ),
        textStyle = MaterialTheme.typography.bodyMedium,
        maxLines = 1,
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done, keyboardType = KeyboardType.Ascii
        ),
        keyboardActions = KeyboardActions(onDone = {
            keyboardController?.hide()
            localFocusManager.clearFocus()
        }),
        visualTransformation = VisualTransformation.None,
    )
}