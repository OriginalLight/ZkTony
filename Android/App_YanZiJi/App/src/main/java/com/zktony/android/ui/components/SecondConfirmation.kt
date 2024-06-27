package com.zktony.android.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.zktony.android.R

@Composable
fun ImportSecondConfirmationDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        title = { Text(stringResource(id = R.string.bring)) },
        text = { Text("将覆盖当前所有参数，无法恢复！！！确认导入吗？") },
        modifier = modifier,
        onDismissRequest = { onDismiss() },
        dismissButton = {
            OutlinedButton(onClick = { onDismiss() }) { Text(stringResource(id = R.string.cancel)) }
        },
        confirmButton = {
            Button(onClick = { onConfirm() }) { Text(stringResource(id = R.string.ok)) }
        }
    )
}