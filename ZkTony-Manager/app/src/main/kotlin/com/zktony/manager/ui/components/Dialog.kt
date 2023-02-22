package com.zktony.manager.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/**
 * @author: 刘贺贺
 * @date: 2023-02-22 14:59
 */
// region InputDialog
// 带输入框的对话框
@Composable
fun InputDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
) {
    
    AlertDialog(
        modifier = Modifier,
        shape = MaterialTheme.shapes.small,
        onDismissRequest = { onDismissRequest() },
        title = {
            Text(text = "软件信息修改")
        },
        text = { /*TODO*/ },
        confirmButton = {
            Button(onClick = { /*TODO*/ }) {
                Text(text = "确定")
            }
        },
        dismissButton = {
            Button(onClick = { /*TODO*/ }) {
                Text(text = "取消")
            }
        },
    )
}
// endregion

// region preview
@Preview
@Composable
fun InputDialogPreview() {
    InputDialog(onDismissRequest = {})
}