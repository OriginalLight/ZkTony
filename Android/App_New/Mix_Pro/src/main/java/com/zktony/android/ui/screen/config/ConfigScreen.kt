package com.zktony.android.ui.screen.config

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.R
import com.zktony.android.ui.components.ZkTonyTopAppBar
import com.zktony.android.ui.navigation.PageEnum
import kotlinx.coroutines.launch

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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    BackHandler {
        if (uiState.page == PageEnum.MAIN) {
            navController.navigateUp()
        } else {
            viewModel.navigationTo(PageEnum.MAIN)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            ZkTonyTopAppBar(
                title = stringResource(id = R.string.system_config),
                navigation = {
                    if (uiState.page == PageEnum.MAIN) {
                        navController.navigateUp()
                    } else {
                        viewModel.navigationTo(PageEnum.MAIN)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.medium
                        ),
                ) {
                    AnimatedVisibility(visible = uiState.page == PageEnum.MAIN) {
                        ConfigMainPage(
                            modifier = Modifier,
                            uiState = uiState,
                            navigationTo = viewModel::navigationTo,
                        )
                    }

                    AnimatedVisibility(visible = uiState.page == PageEnum.TRAVEL_EDIT) {
                        TravelEditPage(
                            modifier = Modifier,
                            navigationTo = viewModel::navigationTo,
                            setTravel = viewModel::setTravel,
                            travel = uiState.settings.travelList.ifEmpty { listOf(0f, 0f, 0f) },
                            showSnackBar = { message ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                        )
                    }

                    AnimatedVisibility(visible = uiState.page == PageEnum.WASTE_EDIT) {
                        WasteEditPage(
                            modifier = Modifier,
                            navigationTo = viewModel::navigationTo,
                            setWaste = viewModel::setWaste,
                            waste = uiState.settings.wasteList.ifEmpty { listOf(0f, 0f, 0f) },
                            showSnackBar = { message ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                        )
                    }
                }
            }
        }
    )
}