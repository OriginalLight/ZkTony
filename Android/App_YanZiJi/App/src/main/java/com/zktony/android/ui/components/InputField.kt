package com.zktony.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.PermContactCalendar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zktony.android.R

@Composable
fun ArgumentsInputField(
    modifier: Modifier = Modifier,
    maxLength: Int = 64,
    showClear: Boolean = true,
    value: String,
    onSetClick: (String) -> Unit
) {
    var text by remember { mutableStateOf(value) }

    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, CircleShape)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showClear && text.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { text = "" },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear"
                )
            }
        }

        BasicTextField(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            value = text,
            onValueChange = {
                if (it.length <= maxLength) {
                    text = it
                }
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge
        )

        Button(
            onClick = { onSetClick(text) }) {
            Text(text = stringResource(id = R.string.set))
        }
    }
}

@Composable
fun UserNameInputField(
    modifier: Modifier = Modifier,
    value: String,
    maxLength: Int = 32,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, CircleShape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PermContactCalendar,
                contentDescription = "Person"
            )
        }

        BasicTextField(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
            value = value,
            onValueChange = {
                if (it.length <= maxLength) {
                    onValueChange(it)
                }
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        if (value.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onValueChange("") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear"
                )
            }
        }
    }
}

@Composable
fun PasswordInputField(
    modifier: Modifier = Modifier,
    value: String,
    maxLength: Int = 32,
    onValueChange: (String) -> Unit
) {
    var showPassword by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, CircleShape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (value.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { showPassword = !showPassword },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.RemoveRedEye,
                    contentDescription = "Show Password"
                )
            }
        } else {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password"
                )
            }
        }

        BasicTextField(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            value = value,
            onValueChange = {
                if (it.length <= maxLength) {
                    onValueChange(it)
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge
        )

        if (value.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onValueChange("") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear"
                )
            }
        }
    }
}


@Preview
@Composable
fun ArgumentsInputGroupPreview() {
    ArgumentsInputField(value = "Hello", onSetClick = {})
}

@Preview
@Composable
fun UserNameInputFieldPreview() {
    UserNameInputField(value = "Hello", onValueChange = {})
}

@Preview
@Composable
fun PasswordInputFieldPreview() {
    PasswordInputField(value = "Hello", onValueChange = {})
}