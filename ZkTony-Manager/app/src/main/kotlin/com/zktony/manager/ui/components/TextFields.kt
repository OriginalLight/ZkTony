package com.zktony.manager.ui.components

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.zktony.manager.common.ext.showShortToast
import com.zktony.manager.data.remote.model.QrCode
import com.zktony.manager.ui.QrCodeActivity
import com.zktony.proto.Software
import com.zktony.proto.software
import com.zktony.www.common.extension.currentTime
import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2023-02-22 15:34
 */

// region QrCodeTextField
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CodeTextField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isQrCode: Boolean = true,
    onSoftwareChange: (Software) -> Unit = {},
) {

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val localFocusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val qrCodeScanner =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val result = it.data?.getStringExtra("SCAN_RESULT")
                // result 是json字符串解析成software对象
                if (isQrCode) {
                    try {
                        val qrCode = Gson().fromJson(result, QrCode::class.java)
                        onSoftwareChange(software {
                            id = qrCode.id
                            package_ = qrCode.`package`
                            versionCode = qrCode.version_code
                            versionName = qrCode.version_name
                            buildType = qrCode.build_type
                            createTime = currentTime()
                        })
                        onValueChange(qrCode.id)
                    } catch (e: Exception) {
                        "二维码格式错误".showShortToast()
                    }
                } else {
                    onValueChange(result ?: "")
                }

            }
        }

    TextField(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Blue.copy(alpha = 0.2f),
                        Color.Blue.copy(alpha = 0.1f),
                        Color.Cyan.copy(alpha = 0.2f),
                    )
                ),
                shape = RoundedCornerShape(4.dp)
            )
            .focusRequester(focusRequester),
        value = value,
        label = { Text(text = label) },
        onValueChange = {
            onValueChange(it)
        },
        leadingIcon = {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = Icons.Outlined.Key,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        },
        trailingIcon = {
            IconButton(
                modifier = Modifier
                    .absoluteOffset(x = (-8).dp),
                onClick = {
                    qrCodeScanner.launch(
                        Intent(
                            context, QrCodeActivity::class.java
                        )
                    )
                }) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.Outlined.QrCode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

        },
        shape = RoundedCornerShape(4.dp),
        colors = TextFieldDefaults.textFieldColors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            containerColor = Color.Transparent,
        ),
        textStyle = MaterialTheme.typography.bodyMedium,
        maxLines = 1,
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Ascii
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
                localFocusManager.clearFocus()
            }),
        visualTransformation = VisualTransformation.None,
    )
}

// endregion

// region CommonTextField
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CommonTextField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit,
    isDone: Boolean = false,
    onDone: () -> Unit = {},
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val localFocusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    TextField(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Blue.copy(alpha = 0.2f),
                        Color.Blue.copy(alpha = 0.1f),
                        Color.Cyan.copy(alpha = 0.2f),
                    )
                ),
                shape = RoundedCornerShape(4.dp)
            )
            .focusRequester(focusRequester),
        value = value,
        label = { Text(text = label) },
        onValueChange = {
            onValueChange(it)
        },
        leadingIcon = {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        },
        shape = RoundedCornerShape(4.dp),
        colors = TextFieldDefaults.textFieldColors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            containerColor = Color.Transparent,
        ),
        textStyle = MaterialTheme.typography.bodyMedium,
        maxLines = if (singleLine) 1 else Int.MAX_VALUE,
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = if (isDone) ImeAction.Done else ImeAction.Next,
            keyboardType = KeyboardType.Text
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                localFocusManager.clearFocus()
            },
            onDone = {
                onDone()
                keyboardController?.hide()
                localFocusManager.clearFocus()
            }
        ),
    )
}
// endregion

// region TimeTextField
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TimeTextField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit = {},
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val localFocusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, y, mon, dayOfMonth ->
            onValueChange("$y-${mon + 1}-$dayOfMonth")
        },
        year,
        month,
        day
    )

    TextField(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Blue.copy(alpha = 0.2f),
                        Color.Blue.copy(alpha = 0.1f),
                        Color.Cyan.copy(alpha = 0.2f),
                    )
                ),
                shape = RoundedCornerShape(4.dp)
            )
            .focusRequester(focusRequester),
        value = value,
        label = { Text(text = label) },
        onValueChange = {
            onValueChange(it)
        },
        leadingIcon = {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        },
        trailingIcon = {
            IconButton(
                modifier = Modifier
                    .absoluteOffset(x = (-8).dp),
                onClick = { datePickerDialog.show() }
            ) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        shape = RoundedCornerShape(4.dp),
        colors = TextFieldDefaults.textFieldColors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            containerColor = Color.Transparent,
        ),
        textStyle = MaterialTheme.typography.bodyMedium,
        maxLines = 1,
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Text
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                onDone()
                keyboardController?.hide()
                localFocusManager.clearFocus()
            }
        ),
    )
}
// endregion