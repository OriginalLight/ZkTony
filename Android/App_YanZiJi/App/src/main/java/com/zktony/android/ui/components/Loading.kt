package com.zktony.android.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ButtonLoading(
    modifier: Modifier = Modifier,
    loading: Boolean
) {
    if (loading) {
        CircularProgressIndicator(
            modifier = modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.size(8.dp))
    }
}