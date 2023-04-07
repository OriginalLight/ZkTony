package com.zktony.manager.ui.fragment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zktony.manager.R
import com.zktony.manager.ui.components.FeatureCard
import com.zktony.manager.ui.components.ManagerAppBar
import com.zktony.manager.ui.viewmodel.SettingPage

/**
 * @author: 刘贺贺
 * @date: 2023-02-23 13:21
 */

@Composable
fun SettingFragment(
    modifier: Modifier = Modifier,
    navigateTo: (SettingPage) -> Unit,
) {

    ManagerAppBar(
        title = stringResource(id = R.string.screen_setting_title),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        LazyVerticalGrid(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            columns = GridCells.Adaptive(minSize = 128.dp)
        ) {
            item {
                FeatureCard(
                    title = "个人信息",
                    icon = Icons.Filled.Person,
                    onClick = { navigateTo(SettingPage.USER_EDIT) }
                )
            }
            item {
                FeatureCard(
                    title = "软件更新",
                    icon = Icons.Default.Upgrade,
                    onClick = { navigateTo(SettingPage.UPGRADE) }
                )
            }
        }
    }
}

@Preview
@Composable
fun SettingPagePreview() {
    SettingFragment(navigateTo = {})
}