package com.zktony.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PermContactCalendar
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zktony.android.R

@Composable
fun ArgumentsSetField(
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
            Text(text = stringResource(id = R.string.app_set))
        }
    }
}

@Composable
fun ArgumentsInputField(
    modifier: Modifier = Modifier,
    maxLength: Int = 64,
    showClear: Boolean = true,
    prefix: String? = null,
    suffix: String? = null,
    value: String,
    shape: Shape = CircleShape,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    onValueChange: (String) -> Unit
) {

    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, shape)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (prefix != null) {
            Text(
                text = prefix,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
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
            singleLine = true,
            textStyle = textStyle,
            keyboardOptions = keyboardOptions
        )

        if (suffix != null) {
            Text(
                text = suffix,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        if (showClear && value.isNotEmpty()) {
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
fun UserNameInputField(
    modifier: Modifier = Modifier,
    value: String,
    maxLength: Int = 32,
    onValueChange: (String) -> Unit
) {
    TextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = {
            if (it.length <= maxLength) {
                onValueChange(it)
            }
        },
        placeholder = {
            Text(text = stringResource(id = R.string.app_username))
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.PermContactCalendar,
                contentDescription = "Person"
            )
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        shape = CircleShape,
        singleLine = true,
        colors = TextFieldDefaults.colors(
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        textStyle = TextStyle(fontSize = 20.sp),
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear"
                    )
                }
            }
        }
    )
}

@Composable
fun PasswordInputField(
    modifier: Modifier = Modifier,
    value: String,
    maxLength: Int = 32,
    onValueChange: (String) -> Unit
) {
    var showPassword by remember { mutableStateOf(false) }

    TextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = {
            if (it.length <= maxLength) {
                onValueChange(it)
            }
        },
        placeholder = {
            Text(text = stringResource(id = R.string.app_password))
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Password"
            )
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Password
        ),
        shape = CircleShape,
        singleLine = true,
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        colors = TextFieldDefaults.colors(
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        textStyle = TextStyle(fontSize = 20.sp),
        trailingIcon = {
            if (value.isNotEmpty()) {
                Row {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = Icons.Default.RemoveRedEye,
                            contentDescription = "Show Password",
                            tint = if (showPassword) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear"
                        )
                    }
                }
            }
        }
    )
}


@Preview
@Composable
fun ArgumentsSetFieldPreview() {
    ArgumentsSetField(value = "Hello", onSetClick = {})
}

@Preview
@Composable
fun ArgumentsInputFieldPreview() {
    ArgumentsInputField(value = "Hello", onValueChange = {})
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