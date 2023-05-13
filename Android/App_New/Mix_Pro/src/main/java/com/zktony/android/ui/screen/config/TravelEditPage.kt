package com.zktony.android.ui.screen.config

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.zktony.android.ui.navigation.PageEnum
import com.zktony.core.ext.Ext
import com.zktony.core.ext.format
import kotlinx.coroutines.delay

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TravelEditPage(
    modifier: Modifier = Modifier,
    navigationTo: (PageEnum) -> Unit = {},
    setTravel: (Float, Float, Float) -> Unit = { _, _, _ -> },
    uiState: ConfigUiState,
    showSnackBar: (String) -> Unit = {},
) {
    var x by remember { mutableStateOf(uiState.travel.first.format()) }
    var y by remember { mutableStateOf(uiState.travel.second.format()) }
    var z by remember { mutableStateOf(uiState.travel.third.format()) }
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
                painter = painterResource(id = R.drawable.ic_distance),
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

        AnimatedVisibility(visible = uiState.travel.first.format() != x || uiState.travel.second.format() != y || uiState.travel.third.format() != z) {
            ElevatedButton(
                modifier = Modifier
                    .width(128.dp)
                    .padding(16.dp),
                onClick = {
                    setTravel(
                        x.toFloatOrNull() ?: 0f,
                        y.toFloatOrNull() ?: 0f,
                        z.toFloatOrNull() ?: 0f
                    )
                    navigationTo(PageEnum.MAIN)
                    showSnackBar(Ext.ctx.getString(R.string.save_success))
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
fun TravelEditPagePreview() {
    TravelEditPage(
        uiState = ConfigUiState()
    )
}