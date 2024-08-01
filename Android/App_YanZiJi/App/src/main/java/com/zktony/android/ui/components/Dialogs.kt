package com.zktony.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.paging.compose.LazyPagingItems
import com.zktony.android.R
import com.zktony.android.data.ProgramQuery
import com.zktony.android.ui.utils.filter
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.android.utils.extra.size
import com.zktony.room.entities.Program
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun LogoutDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.app_logout),
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                text = stringResource(id = R.string.app_logout_content),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    modifier = Modifier.padding(end = 16.dp),
                    onClick = { onDismiss() }
                ) {
                    Text(
                        text = "取消",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Button(
                    onClick = {
                        onDismiss()
                        onLogout()
                    }
                ) {
                    Text(
                        text = "确认",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "删除",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                text = "确认删除所选项吗？",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    modifier = Modifier.padding(end = 16.dp),
                    onClick = { onDismiss() }
                ) {
                    Text(
                        text = "取消",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Button(
                    onClick = {
                        onDismiss()
                        onDelete()
                    }
                ) {
                    Text(
                        text = "确认",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.app_password_modify),
                style = MaterialTheme.typography.titleLarge
            )

            when (step) {
                0 -> {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = oldPassword,
                            onValueChange = {
                                oldPassword = it
                                if (errorOldPasswordMsg.isNotEmpty()) {
                                    errorOldPasswordMsg = ""
                                }
                            },
                            label = { Text("旧密码") },
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
                                        IconButton(onClick = {
                                            showOldPassword = !showOldPassword
                                        }) {
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
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            modifier = Modifier.padding(end = 16.dp),
                            enabled = !loading,
                            onClick = { onDismiss() }) {
                            Text(
                                "取消",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Button(
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
                                Text(
                                    "下一步",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }

                1 -> {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("新密码") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next,
                                keyboardType = KeyboardType.Password
                            ),
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
                                        IconButton(onClick = {
                                            showNewPassword = !showNewPassword
                                        }) {
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
                            label = { Text("确认密码") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done,
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
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            modifier = Modifier.padding(end = 16.dp),
                            enabled = !loading,
                            onClick = { onDismiss() }) {
                            Text(
                                "取消",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Button(
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
                                Text(
                                    "确认",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FileChoiceDialog(
    modifier: Modifier = Modifier,
    files: List<File>,
    onDismiss: () -> Unit,
    onSelected: (File) -> Unit
) {
    var selected by remember { mutableStateOf<File?>(null) }

    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "请选择文件",
                style = MaterialTheme.typography.titleLarge
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                files.forEach {
                    Row(
                        modifier = modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(
                                color = if (selected == it) MaterialTheme.colorScheme.inversePrimary
                                else MaterialTheme.colorScheme.primary.copy(
                                    alpha = 0.1f
                                ),
                                shape = MaterialTheme.shapes.small
                            )
                            .clip(MaterialTheme.shapes.small)
                            .clickable(onClick = { selected = it })
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = it.name,
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Normal)
                        )
                        Text(
                            text = it.size(),
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Normal)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    modifier = Modifier.padding(end = 16.dp),
                    onClick = { onDismiss() }) {
                    Text(
                        "取消",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Button(
                    enabled = selected != null,
                    onClick = {
                        selected?.let { onSelected(it) }
                    }) {
                    Text(
                        "确认",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramQueryDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onQuery: (ProgramQuery) -> Unit
) {
    var name by remember { mutableStateOf<String?>(null) }
    val dateRangePickerState = rememberDateRangePickerState()

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "搜索",
                style = MaterialTheme.typography.titleLarge
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    value = name ?: "",
                    onValueChange = { name = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    label = { Text("程序名称") }
                )
                DateRangePicker(
                    modifier = Modifier.heightIn(max = 450.dp),
                    state = dateRangePickerState
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    modifier = Modifier.padding(end = 16.dp),
                    onClick = { onDismiss() }
                ) {
                    Text(
                        text = "取消",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Button(
                    onClick = {
                        onQuery(
                            ProgramQuery(
                                name = name,
                                startTime = dateRangePickerState.selectedStartDateMillis,
                                endTime = dateRangePickerState.selectedEndDateMillis
                            )
                        )
                    }
                ) {
                    Text(
                        text = "搜索",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun ProgramSelectDialog(
    modifier: Modifier = Modifier,
    entities: LazyPagingItems<Program>,
    onDismiss: () -> Unit,
    onSelect: (Program) -> Unit
) {
    var query by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = query,
                onValueChange = { query = it },
                label = { Text("搜索") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
            )
            LazyColumn(
                modifier = modifier
                    .heightIn(max = 420.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val content = @Composable { index: Int, item: Program ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.small
                            )
                            .clip(MaterialTheme.shapes.small)
                            .clickable(onClick = { onSelect(item) })
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = (index + 1).toString() + ".",
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Normal)
                        )
                        Text(
                            text = item.name,
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Normal),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (query.isEmpty()) {
                    itemsIndexed(entities) { index, item ->
                        content(index, item)
                    }
                } else {
                    itemsIndexed(entities.filter { i -> i.name.contains(query) }) { index, item ->
                        content(index, item)
                    }
                }
            }
        }

    }
}