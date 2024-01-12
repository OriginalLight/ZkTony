package com.zktony.manager.ui.fragment

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.manager.BuildConfig
import com.zktony.manager.ui.components.ManagerAppBar
import com.zktony.manager.ui.viewmodel.SettingPage
import com.zktony.manager.ui.viewmodel.UpgradeViewModel

@Composable
fun UpgradeFragment(
    modifier: Modifier = Modifier,
    navigateTo: (SettingPage) -> Unit,
    viewModel: UpgradeViewModel,
    isDualPane: Boolean = false
) {
    BackHandler {
        navigateTo(SettingPage.SETTING)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()


    ManagerAppBar(
        title = "软件更新",
        isFullScreen = !isDualPane,
        onBack = { navigateTo(SettingPage.SETTING) },
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val application = uiState.application
        if (application != null) {
            if (application.versionCode > BuildConfig.VERSION_CODE) {
                if (uiState.progress == 0) {
                    Text(text = "发现新版本：${application.versionName}")
                    Text(text = "更新内容：${application.description}")
                    Button(
                        modifier = Modifier.padding(top = 16.dp),
                        onClick = { viewModel.upgrade() }) {
                        Text(text = "立即更新")
                    }
                } else {
                    Text(text = "正在下载：${uiState.progress}%")
                }
            } else {
                Text(text = BuildConfig.VERSION_NAME)
                Text(text = "当前已是最新版本")
            }
        } else {
            // 加载中
            CircularProgressIndicator()
        }
    }
}