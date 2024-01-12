package com.zktony.android.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zktony.android.data.entities.internal.OrificePlate
import com.zktony.android.utils.extra.format

/**
 * @author 刘贺贺
 * @date 2023/8/10 14:18
 */
@Composable
fun OrificePlateCard(
    modifier: Modifier = Modifier,
    orificePlate: OrificePlate,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = MaterialTheme.shapes.small
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ElevatedCard(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = "${orificePlate.column} x ${orificePlate.row}",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontStyle = FontStyle.Italic,
                        ),
                    )
                }

            }
            ElevatedCard(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = "${
                            orificePlate.orifices.flatten().count { it.selected }
                        }/${orificePlate.orifices.flatten().size}",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontStyle = FontStyle.Italic,
                        ),
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ElevatedCard(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = if (orificePlate.type == 0) "分液模式" else "混合模式",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontStyle = FontStyle.Italic,
                        ),
                    )
                }
            }

            ElevatedCard(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = "${orificePlate.delay} ms",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontStyle = FontStyle.Italic,
                        ),
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ElevatedCard(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = if (orificePlate.type == 0) orificePlate.getVolume()[0].format(1) else "[${
                            orificePlate.getVolume().joinToString(", ") { it.format(1) }
                        }]",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontStyle = FontStyle.Italic,
                        ),
                    )
                }
            }
        }
    }
}