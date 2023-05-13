package com.zktony.android.ui.screen.motor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ElevatedButton
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
import com.zktony.android.ui.navigation.PageEnum
import com.zktony.core.ext.Ext

@Composable
fun MotorEditPage(
    modifier: Modifier = Modifier,
    entity: Motor = Motor(),
    navigationTo: (PageEnum) -> Unit = {},
    update: (Motor) -> Unit = {},
    showSnackbar: (String) -> Unit = {},
) {
    var speed by remember { mutableStateOf(entity.speed) }
    var acc by remember { mutableStateOf(entity.acc) }
    var dec by remember { mutableStateOf(entity.dec) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(128.dp))

        Text(
            text = entity.text,
            fontSize = 50.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 30.sp,
        )

        Column {
            Text(
                text = stringResource(id = R.string.speed),
                style = MaterialTheme.typography.labelLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier.size(36.dp),
                    painter = painterResource(id = R.drawable.ic_speed),
                    contentDescription = stringResource(id = R.string.speed)
                )

                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
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
                text = stringResource(id = R.string.acceleration),
                style = MaterialTheme.typography.labelLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier.size(36.dp),
                    painter = painterResource(id = R.drawable.ic_acceleration),
                    contentDescription = stringResource(id = R.string.acceleration)
                )

                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
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
                text = stringResource(id = R.string.deceleration),
                style = MaterialTheme.typography.labelLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier.size(36.dp),
                    painter = painterResource(id = R.drawable.ic_deceleration),
                    contentDescription = stringResource(id = R.string.deceleration)
                )

                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
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
            ElevatedButton(
                modifier = Modifier
                    .width(128.dp)
                    .padding(vertical = 16.dp),
                onClick = {
                    update(entity.copy(speed = speed, acc = acc, dec = dec))
                    navigationTo(PageEnum.MAIN)
                    showSnackbar(Ext.ctx.getString(R.string.save_success))
                },
            ) {
                Icon(
                    modifier = Modifier.size(36.dp),
                    imageVector = Icons.Filled.Save,
                    contentDescription = null,
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