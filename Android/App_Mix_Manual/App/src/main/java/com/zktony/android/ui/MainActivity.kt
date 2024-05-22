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

//    companion object {
//        private const val REQUEST_CODE_STORAGE_PERMISSIONS = 1
//    }

    companion object {
        const val PERMISSION_REQUEST_CODE = 1
    }




//    // 定义权限请求的结果处理器
//    private val requestPermissionsLauncher = registerForActivityResult(
//        ActivityResultContracts.RequestMultiplePermissions()
//    ) { permissions ->
//        // 处理权限请求结果
//        val allPermissionsGranted = permissions.entries.all { it.value }
//        if (allPermissionsGranted) {
//            onPermissionsGranted()
//        } else {
//            Toast.makeText(this, "必须授予所有权限才能继续", Toast.LENGTH_SHORT).show()
//        }
//    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(serviceObserver)

        // 检查并请求权限
//        checkAndRequestPermissions()

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


//            fun checkAndRequestPermissions(): Boolean {
//                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//                    != PackageManager.PERMISSION_GRANTED) {
//
//                    ActivityCompat.requestPermissions(
//                        this,
//                        arrayOf(Manifest.permission.CAMERA),
//                        PERMISSION_REQUEST_CODE
//                    )
//                    return false
//                }
//                return true
//            }
        }
    }



//    fun checkAndRequestPermissions() {
//        val permissionsNeeded = mutableListOf<String>()
//
//        // 检查读取外部存储权限
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
//            != PackageManager.PERMISSION_GRANTED) {
//            permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
//        }
//
//        // 检查写入外部存储权限
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//            != PackageManager.PERMISSION_GRANTED) {
//            permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//        }
//
//        // 请求权限
//        if (permissionsNeeded.isNotEmpty()) {
//            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), REQUEST_CODE_STORAGE_PERMISSIONS)
//        } else {
//            // 已经具有所有权限
//            onPermissionsGranted()
//        }
//    }
//
//    private fun onPermissionsGranted() {
//        // 执行需要权限的操作
//        Toast.makeText(this, "所有权限已授予", Toast.LENGTH_SHORT).show()
//        // TODO: 在这里执行你的文件读写操作
//    }
}