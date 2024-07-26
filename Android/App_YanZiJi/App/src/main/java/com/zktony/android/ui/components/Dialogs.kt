package com.zktony.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zktony.android.R
import com.zktony.android.ui.viewmodel.SettingsArgumentsViewModel
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ImportConfirmDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    items: List<File>,
    viewModel: SettingsArgumentsViewModel
) {
    val scope = rememberCoroutineScope()
    var selected by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(false) }

    AlertDialog(
        title = { Text(stringResource(id = R.string.one_click_import)) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (items.size > 1) {
                    VerticalRadioButtonGroup(
                        modifier = if (items.size <= 3) Modifier else Modifier
                            .heightIn(max = 120.dp)
                            .verticalScroll(rememberScrollState()),
                        selected = selected, options = items.map { it.name }
                    ) {
                        selected = it
                    }
                }
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.import_confirm_content),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        },
        modifier = modifier,
        onDismissRequest = { onDismiss() },
        dismissButton = {
            OutlinedButton(onClick = { onDismiss() }) {
                Text(
                    stringResource(id = R.string.cancel),
                    letterSpacing = 12.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        loading = true
                        viewModel.importArguments(items.getOrNull(selected))
                        loading = false
                        onDismiss()
                    }
                }) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }
                Text(
                    stringResource(id = R.string.ok),
                    letterSpacing = 12.sp
                )
            }
        }
    )
}

@Composable
fun ClearConfirmDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    viewModel: SettingsArgumentsViewModel
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }

    AlertDialog(
        title = { Text(stringResource(id = R.string.one_click_clear)) },
        text = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.clear_confirm_content),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        },
        modifier = modifier,
        onDismissRequest = { onDismiss() },
        dismissButton = {
            OutlinedButton(onClick = { onDismiss() }) {
                Text(
                    stringResource(id = R.string.cancel),
                    letterSpacing = 12.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        loading = true
                        viewModel.clearArguments()
                        loading = false
                        onDismiss()
                    }
                }) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }

                Text(
                    stringResource(id = R.string.ok),
                    letterSpacing = 12.sp
                )
            }
        }
    )
}

@Composable
fun LogoutConfirmDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        title = { Text(stringResource(id = R.string.logout)) },
        text = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.logout_confirm_content),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        },
        modifier = modifier,
        onDismissRequest = { onDismiss() },
        dismissButton = {
            OutlinedButton(onClick = { onDismiss() }) {
                Text(
                    stringResource(id = R.string.cancel),
                    letterSpacing = 12.sp
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm() }) {
                Text(
                    stringResource(id = R.string.ok),
                    letterSpacing = 12.sp
                )
            }
        }
    )
}

@Composable
fun PasswordModifyDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onVerifyPassword: suspend (String) -> Boolean,
    onModifyPassword: suspend (String) -> Boolean
) {
    val scope = rememberCoroutineScope()
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showOldPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var errorOldPasswordMsg by remember { mutableStateOf("") }
    var step by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.password_modify),
                style = MaterialTheme.typography.titleLarge
            )

            when (step) {
                0 -> {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = oldPassword,
                        onValueChange = {
                            oldPassword = it
                            if (errorOldPasswordMsg.isNotEmpty()) {
                                errorOldPasswordMsg = ""
                            }
                        },
                        placeholder = { Text("旧密码") },
                        shape = CircleShape,
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 20.sp),
                        visualTransformation = if (showOldPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password"
                            )
                        },
                        trailingIcon = {
                            if (oldPassword.isNotEmpty()) {
                                Row {
                                    IconButton(onClick = { showOldPassword = !showOldPassword }) {
                                        Icon(
                                            imageVector = Icons.Default.RemoveRedEye,
                                            contentDescription = "Show Password",
                                            tint = if (showOldPassword) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    IconButton(onClick = { oldPassword = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear"
                                        )
                                    }
                                }
                            }
                        },
                        isError = errorOldPasswordMsg.isNotEmpty()
                    )

                    if (errorOldPasswordMsg.isNotEmpty()) {
                        Text(
                            text = errorOldPasswordMsg,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .width(120.dp),
                            enabled = !loading,
                            onClick = { onDismiss() }) {
                            Text("取消")
                        }

                        Button(
                            modifier = Modifier.width(120.dp),
                            enabled = oldPassword.isNotEmpty() && !loading,
                            onClick = {
                                scope.launch {
                                    loading = true
                                    val res = onVerifyPassword(oldPassword)
                                    loading = false
                                    if (res) {
                                        step = 1
                                    } else {
                                        errorOldPasswordMsg = "密码错误"
                                    }
                                }
                            }) {
                            ButtonLoading(loading = loading) {
                                Text("下一步")
                            }
                        }
                    }
                }

                1 -> {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        placeholder = { Text("新密码") },
                        shape = CircleShape,
                        singleLine = true,
                        visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        textStyle = TextStyle(fontSize = 20.sp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password"
                            )
                        },
                        trailingIcon = {
                            if (newPassword.isNotEmpty()) {
                                Row {
                                    IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                        Icon(
                                            imageVector = Icons.Default.RemoveRedEye,
                                            contentDescription = "Show Password",
                                            tint = if (showNewPassword) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    IconButton(onClick = { newPassword = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear"
                                        )
                                    }
                                }
                            }
                        }
                    )

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            if (errorOldPasswordMsg.isNotEmpty()) {
                                errorOldPasswordMsg = ""
                            }
                        },
                        placeholder = { Text("确认密码") },
                        shape = CircleShape,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Password
                        ),
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        textStyle = TextStyle(fontSize = 20.sp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password"
                            )
                        },
                        trailingIcon = {
                            if (confirmPassword.isNotEmpty()) {
                                Row {
                                    IconButton(onClick = {
                                        showConfirmPassword = !showConfirmPassword
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.RemoveRedEye,
                                            contentDescription = "Show Password",
                                            tint = if (showConfirmPassword) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    IconButton(onClick = { confirmPassword = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear"
                                        )
                                    }
                                }
                            }
                        },
                        isError = errorOldPasswordMsg.isNotEmpty()
                    )

                    if (errorOldPasswordMsg.isNotEmpty()) {
                        Text(
                            text = errorOldPasswordMsg,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .width(120.dp),
                            enabled = !loading,
                            onClick = { onDismiss() }) {
                            Text("取消")
                        }

                        Button(
                            modifier = Modifier.width(120.dp),
                            enabled = newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && !loading,
                            onClick = {
                                scope.launch {
                                    loading = true
                                    if (newPassword != confirmPassword) {
                                        errorOldPasswordMsg = "两次密码不一致"
                                        loading = false
                                        return@launch
                                    }
                                    val res = onModifyPassword(newPassword)
                                    loading = false
                                    if (res) {
                                        onDismiss()
                                    } else {
                                        errorOldPasswordMsg = "修改失败"
                                    }
                                }
                            }) {
                            ButtonLoading(loading = loading) {
                                Text("确认")
                            }
                        }
                    }
                }
            }
        }
    }
}