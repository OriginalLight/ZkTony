package com.zktony.android.ui.screen.motor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.R
import com.zktony.android.data.entity.Motor
import com.zktony.android.ui.components.ZkTonyTopAppBar
import com.zktony.android.ui.viewmodel.MotorPage
import com.zktony.android.ui.viewmodel.MotorViewModel

/**
 * Motor screen
 *
 * @param modifier Modifier
 * @param navController NavHostController
 * @param viewModel MotorViewModel
 * @return Unit
 */
@Composable
fun MotorScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: MotorViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var entity by remember { mutableStateOf(Motor()) }

    BackHandler {
        if (uiState.page == MotorPage.MOTOR_EDIT) {
            viewModel.navigateTo(MotorPage.MOTOR)
        } else {
            navController.navigateUp()
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
            title = stringResource(id = R.string.motor_config),
            onBack = {
                if (uiState.page == MotorPage.MOTOR_EDIT) {
                    viewModel.navigateTo(MotorPage.MOTOR)
                } else {
                    navController.navigateUp()
                }
            }
        )
        AnimatedVisibility(visible = uiState.page == MotorPage.MOTOR) {
            MotorPage(
                modifier = Modifier,
                list = uiState.list,
                edit = {
                    entity = it
                    viewModel.navigateTo(MotorPage.MOTOR_EDIT)
                }
            )
        }
        AnimatedVisibility(visible = uiState.page == MotorPage.MOTOR_EDIT) {
            MotorEditPage(
                modifier = Modifier,
                entity = entity,
                update = {
                    viewModel.update(it)
                    viewModel.navigateTo(MotorPage.MOTOR)
                }
            )
        }
    }
}