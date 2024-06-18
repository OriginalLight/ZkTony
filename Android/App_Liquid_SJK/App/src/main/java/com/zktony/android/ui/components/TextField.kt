package com.zktony.android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zktony.android.data.entities.internal.Point
import com.zktony.android.utils.extra.format

/**
 * @author 刘贺贺
 * @date 2023/8/8 13:56
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CoordinateInput(
    modifier: Modifier = Modifier,
    title: String,
    point: Point = Point(),
    limit: Point? = null,
    onCoordinateChange: (Point) -> Unit = {},
    onClick: () -> Unit = {}
) {
    val softKeyboard = LocalSoftwareKeyboardController.current
    var abscissa by remember { mutableStateOf(point.x.format(1)) }
    var ordinate by remember { mutableStateOf(point.y.format(1)) }

    ElevatedCard(modifier = modifier) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                modifier = Modifier.width(100.dp),
                text = title,
                fontStyle = FontStyle.Italic,
                fontSize = 20.sp,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                BasicTextField(
                    value = abscissa,
                    onValueChange = {
                        abscissa = it
                        onCoordinateChange(
                            Point(
                                abscissa.toDoubleOrNull() ?: 0.0,
                                ordinate.toDoubleOrNull() ?: 0.0
                            )
                        )
                    },
                    textStyle = TextStyle(
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            softKeyboard?.hide()
                        }
                    ),
                    decorationBox = @Composable { innerTextField ->
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "横坐标",
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Serif,
                                    color = Color.Gray
                                )
                                innerTextField()
                                if (limit != null) {
                                    Text(
                                        text = "<= ${limit.x}",
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.Serif,
                                        color = Color.Gray
                                    )
                                }
                            }
                            Divider()
                        }
                    }
                )

                BasicTextField(
                    value = ordinate,
                    onValueChange = {
                        ordinate = it
                        onCoordinateChange(
                            Point(
                                abscissa.toDoubleOrNull() ?: 0.0,
                                ordinate.toDoubleOrNull() ?: 0.0
                            )
                        )
                    },
                    textStyle = TextStyle(
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            softKeyboard?.hide()
                        }
                    ),
                    decorationBox = @Composable { innerTextField ->
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "纵坐标",
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Serif,
                                    color = Color.Gray
                                )
                                innerTextField()
                                if (limit != null) {
                                    Text(
                                        text = "<= ${limit.y}",
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.Serif,
                                        color = Color.Gray
                                    )
                                }
                            }
                            Divider()
                        }
                    }
                )
            }

            IconButton(
                onClick = {
                    softKeyboard?.hide()
                    onClick()
                },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
fun CircleTextField(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Decimal,
    onValueChange: (String) -> Unit = {},
) {
    val softKeyboard = LocalSoftwareKeyboardController.current

    TextField(
        modifier = modifier.fillMaxWidth(),
        value = TextFieldValue(value, TextRange(value.length)),
        onValueChange = {
            onValueChange(it.text)
        },
        leadingIcon = {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = title,
                fontStyle = FontStyle.Italic,
                fontSize = 20.sp,
                fontFamily = FontFamily.Serif,
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                softKeyboard?.hide()
            }
        ),
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
        ),
        textStyle = TextStyle(
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            fontFamily = FontFamily.Monospace,
        ),
    )
}

@Preview
@Composable
fun CoordinateInputPreview() {
    CoordinateInput(
        title = "坐标",
        limit = Point(10.0, 10.0),
        point = Point(1.0, 1.0)
    )
}

@Preview
@Composable
fun CircleTextFieldPreview() {
    CircleTextField(
        title = "半径",
        value = "1.0"
    )
}