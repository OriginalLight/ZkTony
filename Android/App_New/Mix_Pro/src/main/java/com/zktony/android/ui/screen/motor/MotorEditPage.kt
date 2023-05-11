package com.zktony.android.ui.screen.motor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zktony.android.R
import com.zktony.android.data.entity.Motor

/**
 * Motor edit page
 *
 * @param modifier Modifier
 * @param entity Motor
 * @param update Function1<Motor, Unit>
 * @return Unit
 */
@Composable
fun MotorEditPage(
    modifier: Modifier = Modifier,
    entity: Motor,
    update: (Motor) -> Unit = {},
) {
    var speed by remember { mutableStateOf(entity.speed) }
    var acc by remember { mutableStateOf(entity.acc) }
    var dec by remember { mutableStateOf(entity.dec) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(vertical = 8.dp),
            text = entity.text,
            fontSize = 50.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 30.sp,
        )
        Column {
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = stringResource(id = R.string.speed),
                style = MaterialTheme.typography.labelLarge
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(8.dp),
                    painter = painterResource(id = R.drawable.ic_speed),
                    contentDescription = stringResource(id = R.string.speed)
                )
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = speed.toString(),
                    style = MaterialTheme.typography.bodyLarge
                )
                Slider(
                    value = speed.toFloat(),
                    onValueChange = { speed = it.toInt() },
                    valueRange = 0f..600f,
                )
            }
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = stringResource(id = R.string.acceleration),
                style = MaterialTheme.typography.labelLarge
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(8.dp),
                    painter = painterResource(id = R.drawable.ic_acceleration),
                    contentDescription = stringResource(id = R.string.acceleration)
                )
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = acc.toString(),
                    style = MaterialTheme.typography.bodyLarge
                )
                Slider(
                    value = acc.toFloat(),
                    onValueChange = { acc = it.toInt() },
                    valueRange = 10f..100f,
                )
            }
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = stringResource(id = R.string.deceleration),
                style = MaterialTheme.typography.labelLarge
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(8.dp),
                    painter = painterResource(id = R.drawable.ic_deceleration),
                    contentDescription = stringResource(id = R.string.deceleration)
                )
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = dec.toString(),
                    style = MaterialTheme.typography.bodyLarge
                )
                Slider(
                    value = dec.toFloat(),
                    onValueChange = { dec = it.toInt() },
                    valueRange = 10f..100f,
                )
            }
        }

        AnimatedVisibility(visible = entity.speed != speed || entity.acc != acc || entity.dec != dec) {
            FloatingActionButton(
                modifier = Modifier
                    .width(128.dp)
                    .padding(16.dp),
                onClick = { update(entity.copy(speed = speed, acc = acc, dec = dec)) },
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
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun MotorEditPagePreview() {
    MotorEditPage(
        modifier = Modifier,
        entity = Motor(text = "M1")
    )
}