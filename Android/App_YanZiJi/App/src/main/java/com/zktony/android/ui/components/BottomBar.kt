package com.zktony.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zktony.android.utils.Constants
import com.zktony.datastore.rememberDataSaverState

@Composable
fun BottomBar(modifier: Modifier = Modifier) {
    // S/N
    val sn by rememberDataSaverState(key = Constants.SN, initialValue = "Unknown", senseExternalDataChange = true)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(color = MaterialTheme.colorScheme.tertiaryContainer)
            .padding(horizontal = 8.dp),
    ) {
        // Bottom bar content
        Tips(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .height(48.dp)
        )

        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "S/N: $sn",
            style = MaterialTheme.typography.bodyLarge
        )

        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Connection()
            Time()
        }
    }
}