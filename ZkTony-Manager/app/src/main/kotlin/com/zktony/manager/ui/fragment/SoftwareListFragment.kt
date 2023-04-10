package com.zktony.manager.ui.fragment

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.gson.Gson
import com.zktony.manager.ext.showShortToast
import com.zktony.manager.data.remote.model.QrCode
import com.zktony.manager.ui.QrCodeActivity
import com.zktony.manager.ui.components.ManagerAppBar
import com.zktony.manager.ui.components.SoftwareCard
import com.zktony.manager.ui.components.SoftwareSearchBar
import com.zktony.manager.ui.viewmodel.ManagerPageEnum
import com.zktony.manager.ui.viewmodel.SoftwareViewModel
import com.zktony.proto.software
import com.zktony.www.common.extension.currentTime

@Composable
fun SoftwareListFragment(
    modifier: Modifier = Modifier,
    navigateTo: (ManagerPageEnum) -> Unit,
    viewModel: SoftwareViewModel,
    isDualPane: Boolean = false
) {

    BackHandler {
        navigateTo(ManagerPageEnum.MANAGER)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val qrCodeScanner =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val result = it.data?.getStringExtra("SCAN_RESULT")
                // result 是json字符串解析成software对象
                try {
                    val qrCode = Gson().fromJson(result, QrCode::class.java)
                    viewModel.insert(software {
                        id = qrCode.id
                        package_ = qrCode.`package`
                        versionCode = qrCode.version_code
                        versionName = qrCode.version_name
                        buildType = qrCode.build_type
                        createTime = currentTime()
                    })
                } catch (e: Exception) {
                    "二维码格式错误".showShortToast()
                }

            }
        }


    Column(
        modifier = modifier
    ) {
        val isSearchExpanded = remember { mutableStateOf(false) }

        ManagerAppBar(
            title = "软件列表",
            isFullScreen = !isDualPane,
            onBack = { navigateTo(ManagerPageEnum.MANAGER) },
            actions = {

                Row {
                    FilledIconButton(
                        onClick = { isSearchExpanded.value = !isSearchExpanded.value },
                        modifier = Modifier.padding(8.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = if (isSearchExpanded.value) Icons.Outlined.Close else Icons.Filled.Search,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    FilledIconButton(
                        onClick = {
                            qrCodeScanner.launch(
                                Intent(
                                    context, QrCodeActivity::class.java
                                )
                            )
                        },
                        modifier = Modifier.padding(8.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            })

        // 搜索栏
        AnimatedVisibility(visible = isSearchExpanded.value) {
            SoftwareSearchBar(
                modifier = Modifier,
                onSearch = {
                    viewModel.search(it)
                    isSearchExpanded.value = !isSearchExpanded.value
                },
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyVerticalGrid(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                columns = GridCells.Adaptive(minSize = 128.dp)
            ) {
                uiState.list.forEach {
                    item {
                        SoftwareCard(
                            software = it,
                            onClick = {
                                navigateTo(ManagerPageEnum.SOFTWARE_EDIT)
                                viewModel.setSoftware(it)
                            }
                        )
                    }
                }
            }
        }
    }
}