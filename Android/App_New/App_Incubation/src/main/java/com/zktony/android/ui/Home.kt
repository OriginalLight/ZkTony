package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.R
import com.zktony.android.ui.navigation.AppNavigation
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.utils.NavigationContentPosition
import com.zktony.android.ui.utils.PageType
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: HomeViewModel,
    selectedDestination: String,
    navigationActions: NavigationActions,
    snackbarHostState: SnackbarHostState,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    BackHandler {
        scope.launch {
            when (uiState.page) {
                PageType.START -> viewModel.uiEvent(HomeUiEvent.NavTo(PageType.LIST))
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Image(
                    modifier = Modifier.height(48.dp),
                    painter = painterResource(id = R.mipmap.logo),
                    contentDescription = null
                )
            })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Row(modifier = modifier.padding(paddingValues)) {
            AppNavigation(
                selectedDestination = selectedDestination,
                navigationContentPosition = NavigationContentPosition.CENTER,
                navigateToTopLevelDestination = navigationActions::navigateTo,
                onBackPressed = {
                    scope.launch {
                        when (uiState.page) {
                            PageType.START -> viewModel.uiEvent(HomeUiEvent.NavTo(PageType.LIST))
                            else -> {}
                        }
                    }
                }
            )
            HomeScreen(
                navController = navController,
                uiState = uiState,
                uiEvent = viewModel::uiEvent
            )
        }
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    uiState: HomeUiState,
    uiEvent: (HomeUiEvent) -> Unit,
) {

}