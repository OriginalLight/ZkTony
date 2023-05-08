package com.zktony.android.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.R
import com.zktony.android.ui.components.ZkTonyTopAppBar
import com.zktony.android.ui.viewmodel.ConfigUiState
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
        ConfigForm(
            modifier = Modifier,
            uiState = uiState
        )

    }
}

@Composable
fun ConfigForm(
    modifier: Modifier = Modifier,
    uiState: ConfigUiState,
) {
    val lazyColumnState = rememberLazyListState()

    val list = listOf(
        Triple(Pair(R.drawable.ic_distance, R.string.x_axis_travel), uiState.xAxisTravel, { }),
        Triple(Pair(R.drawable.ic_distance, R.string.y_axis_travel), uiState.yAxisTravel, { }),
        Triple(Pair(R.drawable.ic_distance, R.string.z_axis_travel), uiState.zAxisTravel, { }),
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 8.dp, horizontal = 128.dp),
        state = lazyColumnState,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        list.forEach {
            item {
                Card(
                    modifier = Modifier
                        .wrapContentHeight()
                        .clickable { it.third() },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            modifier = Modifier
                                .size(48.dp)
                                .padding(start = 16.dp),
                            painter = painterResource(id = it.first.first),
                            contentDescription = null,
                        )
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = stringResource(id = it.first.second),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            modifier = Modifier.padding(end = 16.dp),
                            text = String.format("%.2f", it.second),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 960)
fun ConfigFormPreview() {
    ConfigForm(
        uiState = ConfigUiState(
            xAxisTravel = 0f,
            yAxisTravel = 0f,
            zAxisTravel = 0f,
        )
    )
}
