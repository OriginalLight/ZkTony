package com.zktony.android.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.zktony.android.data.datastore.DataSaverDataStore
import com.zktony.android.data.datastore.LocalDataSaver
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.theme.AppTheme
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.LocalSnackbarHostState
import com.zktony.android.utils.service.ServiceFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The main activity of the application.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var serviceFactory: ServiceFactory

    @Inject
    lateinit var dataSaverDataStore: DataSaverDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        serviceFactory.create()

        setContent {

            val navController = rememberNavController()
            val navigationActions = remember(navController) { NavigationActions(navController) }
            val snackbarHostState = remember { SnackbarHostState() }
            val homeViewModel: HomeViewModel = hiltViewModel()

            CompositionLocalProvider(
                LocalNavigationActions provides navigationActions,
                LocalSnackbarHostState provides snackbarHostState,
                LocalDataSaver provides dataSaverDataStore
            ) {
                AppTheme {
                    AppNavigation(
                        navController = navController,
                        homeViewModel = homeViewModel,
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }
}