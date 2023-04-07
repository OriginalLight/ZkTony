package com.zktony.manager.ui.fragment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zktony.manager.ui.components.FeatureCard
import com.zktony.manager.ui.components.ManagerAppBar
import com.zktony.manager.ui.viewmodel.ManagerPageEnum

/**
 * @author: 刘贺贺
 * @date: 2023-02-23 13:11
 */

// region ManagerPage
@Composable
fun ManagerFragment(
    modifier: Modifier = Modifier,
    navigateTo: (ManagerPageEnum) -> Unit,
) {
    ManagerAppBar(
        title = "信息管理",
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
                    title = "客户管理",
                    icon = Icons.Default.Person,
                    onClick = { navigateTo(ManagerPageEnum.CUSTOMER_LIST) }
                )
            }
            item {
                FeatureCard(
                    title = "仪器管理",
                    icon = Icons.Default.LaptopMac,
                    onClick = { navigateTo(ManagerPageEnum.INSTRUMENT_LIST) }
                )
            }
            item {
                FeatureCard(
                    title = "软件管理",
                    icon = Icons.Default.Android,
                    onClick = { navigateTo(ManagerPageEnum.SOFTWARE_LIST) }
                )
            }
        }
    }
}
// endregion

// region Preview
@Preview
@Composable
fun ManagerPagePreview() {
    ManagerFragment(navigateTo = {})
}
// endregion