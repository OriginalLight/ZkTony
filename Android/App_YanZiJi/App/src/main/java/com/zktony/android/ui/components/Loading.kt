package com.zktony.android.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ButtonLoading(
    modifier: Modifier = Modifier,
    loading: Boolean,
    size: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    if (loading) {
        CircularProgressIndicator(
            modifier = modifier.size(size),
            strokeWidth = 2.dp
        )
    } else {
        content()
    }
}

@Composable
fun IconLoading(
    modifier: Modifier = Modifier,
    loading: Boolean,
    size: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    if (loading) {
        CircularProgressIndicator(
            modifier = modifier
                .padding(end = 8.dp)
                .size(size),
            strokeWidth = 2.dp
        )
    } else {
        content()
    }
}