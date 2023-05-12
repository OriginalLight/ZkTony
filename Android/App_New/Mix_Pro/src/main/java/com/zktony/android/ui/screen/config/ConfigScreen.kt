package com.zktony.android.ui.screen.config

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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

/**
 * 系统配置
 *
 * @param modifier Modifier
 * @param navController NavHostController
 * @param viewModel ConfigViewModel
 * @return Unit
 */
@Composable
fun ConfigScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ConfigViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        if (uiState.page == ConfigPage.CONFIG) {
            navController.navigateUp()
        } else {
            viewModel.navigateTo(ConfigPage.CONFIG)
        }
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
                if (uiState.page == ConfigPage.CONFIG) {
                    navController.navigateUp()
                } else {
                    viewModel.navigateTo(ConfigPage.CONFIG)
                }
            }
        )
        AnimatedVisibility(visible = uiState.page == ConfigPage.CONFIG) {
            ConfigPage(
                modifier = Modifier,
                uiState = uiState,
                navigationTo = viewModel::navigateTo
            )
        }
        AnimatedVisibility(visible = uiState.page == ConfigPage.TRAVEL_EDIT) {
            TravelEditPage(
                modifier = Modifier,
                uiState = uiState,
                setTravel = { x, y, z ->
                    viewModel.setTravel(x, y, z)
                    viewModel.navigateTo(ConfigPage.CONFIG)
                },
            )
        }

        AnimatedVisibility(visible = uiState.page == ConfigPage.WASTE_EDIT) {
            WasteEditPage(
                modifier = Modifier,
                uiState = uiState,
                setWaste = { x, y, z ->
                    viewModel.setWaste(x, y, z)
                    viewModel.navigateTo(ConfigPage.CONFIG)
                },
            )
        }
    }
}