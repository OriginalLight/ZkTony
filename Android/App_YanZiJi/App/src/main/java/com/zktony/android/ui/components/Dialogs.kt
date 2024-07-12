package com.zktony.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        },
        confirmButton = {
            OutlinedButton(onClick = { onDismiss() }) {
                Text(
                    stringResource(id = R.string.cancel),
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
        },
        confirmButton = {
            OutlinedButton(onClick = { onDismiss() }) {
                Text(
                    stringResource(id = R.string.cancel),
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
            Button(onClick = { onConfirm() }) {
                Text(
                    stringResource(id = R.string.ok),
                    letterSpacing = 12.sp
                )
            }
        },
        confirmButton = {
            OutlinedButton(onClick = { onDismiss() }) {
                Text(
                    stringResource(id = R.string.cancel),
                    letterSpacing = 12.sp
                )
            }
        }
    )
}