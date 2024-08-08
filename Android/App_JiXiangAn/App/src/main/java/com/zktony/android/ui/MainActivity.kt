package com.zktony.android.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.zktony.android.data.datastore.DataSaverDataStore
import com.zktony.android.data.datastore.LocalDataSaver
import com.zktony.android.ui.navigation.AppNavigation
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.theme.AppTheme
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.LocalSnackbarHostState
import com.zktony.android.ui.utils.Permissions
import com.zktony.android.ui.utils.PermissionsScreen
import com.zktony.android.utils.service.ServiceObserver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The main activity of the application.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var serviceObserver: ServiceObserver

    @Inject
    lateinit var dataSaverDataStore: DataSaverDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(serviceObserver)

        setContent {
            val navController = rememberNavController()
            val navigationActions = remember(navController) { NavigationActions(navController) }
            val snackbarHostState = remember { SnackbarHostState() }
            val homeViewModel: HomeViewModel = hiltViewModel()

            CompositionLocalProvider(
                LocalDataSaver provides dataSaverDataStore,
                LocalNavigationActions provides navigationActions,
                LocalSnackbarHostState provides snackbarHostState
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