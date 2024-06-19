package com.zktony.android.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun HomeRoute(viewModel: HomeViewModel) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Logo(modifier = Modifier.align(Alignment.TopStart))
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp)
        ) {
            QrCode(modifier = Modifier.align(Alignment.BottomEnd))
            WellCome(modifier = Modifier.align(Alignment.BottomStart))
        }
    }
}
