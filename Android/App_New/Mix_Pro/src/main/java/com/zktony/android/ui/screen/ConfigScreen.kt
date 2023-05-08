package com.zktony.android.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.R
import com.zktony.android.ui.components.ZkTonyTopAppBar
import com.zktony.android.ui.viewmodel.ConfigViewModel

/**
 * 系统配置
 *
 * @param modifier Modifier
 * @param viewModel ConfigViewModel
 * @param navController NavHostController
 * @return Unit
 */
@Composable
fun ConfigScreen(
    modifier: Modifier = Modifier,
    viewModel: ConfigViewModel,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        navController.navigateUp()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.medium
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ZkTonyTopAppBar(
            title = stringResource(id = R.string.system_config),
            onBack = {
                navController.navigateUp()
            }
        )

    }
}
