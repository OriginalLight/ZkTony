package com.zktony.android.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.zktony.android.ui.components.system.Language
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.theme.AppTheme
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.LocalSnackbarHostState
import com.zktony.android.utils.service.BackgroundService
import com.zktony.datastore.DataSaverDataStore
import com.zktony.datastore.LocalDataSaver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The main activity of the application.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var backgroundService: BackgroundService

    @Inject
    lateinit var dataSaverDataStore: DataSaverDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Add the service observer to the lifecycle
        lifecycle.addObserver(backgroundService)

        setContent {

            val navController = rememberNavController()
            val navigationActions = remember(navController) { NavigationActions(navController) }
            val snackbarHostState = remember { SnackbarHostState() }

            CompositionLocalProvider(
                LocalDataSaver provides dataSaverDataStore,
                LocalNavigationActions provides navigationActions,
                LocalSnackbarHostState provides snackbarHostState
            ) {
                AppTheme {
                    Language {
                        ZktyApp(
                            navController = navController,
                            snackbarHostState = snackbarHostState
                        )
                    }
                }
            }
        }
    }
}