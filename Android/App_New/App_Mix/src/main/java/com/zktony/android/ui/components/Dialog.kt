package com.zktony.android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zktony.android.R

/**
 * @author 刘贺贺
 * @date 2023/6/2 13:21
 */

@Composable
fun InputDialog(
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit,
) {
    var textFieldValue by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Dialog(onDismissRequest = onCancel) {
        ElevatedCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(id = R.string.add),
                    style = MaterialTheme.typography.titleMedium,
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    textStyle = TextStyle(
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp
                    ),
                    singleLine = true,
                    shape = CircleShape,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        onConfirm(textFieldValue)
                    }),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        onClick = { onConfirm(textFieldValue) },
                        enabled = textFieldValue.isNotBlank()
                    ) {
                        Text(text = stringResource(id = R.string.confirm))
                    }

                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        onClick = onCancel,
                    ) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                }
            }
        }
    }
}