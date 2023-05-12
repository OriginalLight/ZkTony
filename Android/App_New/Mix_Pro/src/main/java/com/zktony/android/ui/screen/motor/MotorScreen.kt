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
import com.zktony.android.ui.navigation.PageEnum

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
    val entities by viewModel.entities().collectAsStateWithLifecycle(emptyList())
    var index by remember { mutableStateOf(-1) }
    var page by remember { mutableStateOf(PageEnum.MAIN) }

    BackHandler {
        if (page == PageEnum.MAIN) {
            navController.navigateUp()
        } else {
            page = PageEnum.MAIN
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
                if (page == PageEnum.MAIN) {
                    navController.navigateUp()
                } else {
                    page = PageEnum.MAIN
                }
            }
        )

        AnimatedVisibility(visible = page == PageEnum.MAIN) {
            MotorMainPage(
                modifier = Modifier,
                entities = entities,
                navigationTo = { page = it },
                toggleIndex = { index = it },
            )
        }

        AnimatedVisibility(visible = page == PageEnum.EDIT) {
            MotorEditPage(
                modifier = Modifier,
                entity = entities[index],
                navigationTo = { page = it },
                update = viewModel::update,
            )
        }
    }
}