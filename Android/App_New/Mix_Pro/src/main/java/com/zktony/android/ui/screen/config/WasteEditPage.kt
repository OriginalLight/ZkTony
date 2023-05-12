package com.zktony.android.ui.screen.config

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zktony.android.R
import com.zktony.android.ui.components.CustomTextField
import com.zktony.core.ext.format
import kotlinx.coroutines.delay

/**
 * WasteEditPage
 *
 * @param modifier Modifier
 * @param setWaste Function3<Float, Float, Float, Unit>
 * @param uiState ConfigUiState
 * @return Unit
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WasteEditPage(
    modifier: Modifier = Modifier,
    setWaste: (Float, Float, Float) -> Unit = { _, _, _ -> },
    uiState: ConfigUiState,
) {
    var x by remember { mutableStateOf(uiState.waste.first.format()) }
    var y by remember { mutableStateOf(uiState.waste.second.format()) }
    var z by remember { mutableStateOf(uiState.waste.third.format()) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val softKeyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        delay(100) //延迟操作(关键点)
        focusRequester.requestFocus()
        softKeyboard?.show()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(128.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                modifier = Modifier
                    .size(84.dp)
                    .padding(end = 16.dp),
                painter = painterResource(id = R.drawable.ic_coordinate),
                contentDescription = null,
            )
            Text(
                text = "(",
                fontSize = 30.sp,
            )
            CustomTextField(
                modifier = Modifier
                    .width(128.dp)
                    .focusRequester(focusRequester),
                value = TextFieldValue(x.format(), TextRange(x.format().length)),
                onValueChange = { x = it.text },
                textStyle = TextStyle(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Next)
                })
            )
            Text(
                text = ",",
                fontSize = 30.sp,
            )
            CustomTextField(
                modifier = Modifier.width(128.dp),
                value = TextFieldValue(y.format(), TextRange(y.format().length)),
                onValueChange = { y = it.text },
                textStyle = TextStyle(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Next)
                })
            )
            Text(
                text = ",",
                fontSize = 30.sp,
            )
            CustomTextField(
                modifier = Modifier.width(128.dp),
                value = TextFieldValue(z.format(), TextRange(z.format().length)),
                onValueChange = { z = it.text },
                textStyle = TextStyle(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                keyboardActions = KeyboardActions(onDone = {
                    softKeyboard?.hide()
                })
            )
            Text(
                text = ")",
                fontSize = 30.sp,
            )
        }
        AnimatedVisibility(visible = uiState.waste.first.format() != x || uiState.waste.second.format() != y || uiState.waste.third.format() != z) {
            FloatingActionButton(
                modifier = Modifier
                    .width(128.dp)
                    .padding(16.dp),
                onClick = {
                    setWaste(
                        x.toFloatOrNull() ?: 0f,
                        y.toFloatOrNull() ?: 0f,
                        z.toFloatOrNull() ?: 0f
                    )
                },
            ) {
                Icon(
                    modifier = Modifier.size(36.dp),
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = stringResource(id = R.string.save),
                )
            }
        }
    }
}


@Composable
@Preview(showBackground = true, widthDp = 960)
fun WasteEditPagePreview() {
    WasteEditPage(
        uiState = ConfigUiState()
    )
}