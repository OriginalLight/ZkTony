package com.zktony.manager.ui.screen.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zktony.manager.R
import com.zktony.manager.ui.components.FunctionCard
import com.zktony.manager.ui.components.ManagerAppBar
import com.zktony.manager.ui.screen.SettingPage
import com.zktony.manager.ui.screen.SettingUiState

/**
 * @author: 刘贺贺
 * @date: 2023-02-23 13:21
 */

// region: SettingPage
@Composable
fun SettingPage(
    modifier: Modifier = Modifier,
    uiState: SettingUiState,
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
        FunctionCard(
            title = uiState.user.name.ifEmpty { "姓名" },
            subtitle = uiState.user.phone.ifEmpty { "手机号" },
            icon = Icons.Filled.Person,
            onClick = { navigateTo(SettingPage.USER_MODIFY) },
            shape = RoundedCornerShape(8.dp),
        )
    }
}
// endregion

// region: Preview
@Preview
@Composable
fun SettingPagePreview() {
    SettingPage(
        uiState = SettingUiState(),
        navigateTo = {}
    )
}
// endregion