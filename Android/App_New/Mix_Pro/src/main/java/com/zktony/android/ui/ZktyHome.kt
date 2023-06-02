package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController


@Composable
fun ZktyHome(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ZktyHomeViewModel,
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val content = LocalContext.current

    BackHandler {}

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.clickable {
                launchRuntimeActivity(content, 1L)
            },
            text = "Home",
            style = MaterialTheme.typography.titleLarge,
        )
    }
}
