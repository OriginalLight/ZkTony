package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zktony.android.R
import com.zktony.android.ui.components.ArgumentsSetField
import com.zktony.android.ui.components.DropDownBox
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.zktyBrush
import com.zktony.android.ui.viewmodel.SettingsArgumentsEquipmentViewModel
import com.zktony.android.utils.Constants
import com.zktony.android.utils.ProductUtils
import com.zktony.datastore.rememberDataSaverState

@Composable
fun SettingsArgumentsEquipmentView(viewModel: SettingsArgumentsEquipmentViewModel = hiltViewModel()) {

    val navigationActions = LocalNavigationActions.current

    BackHandler {
        // 拦截返回键
        navigationActions.navigateUp()
    }

    // P/N参数
    var pn by rememberDataSaverState(key = Constants.PN, initialValue = Constants.DEFAULT_PN)
    // S/N参数
    var sn by rememberDataSaverState(key = Constants.SN, initialValue = Constants.DEFAULT_SN)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SettingsArgumentsEquipmentTopBar(navigationActions = navigationActions)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
                .padding(16.dp)
                .clip(MaterialTheme.shapes.medium),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsRow(title = stringResource(id = R.string.product_number)) {
                DropDownBox(
                    modifier = Modifier
                        .width(200.dp)
                        .height(48.dp),
                    selected = ProductUtils.ProductNumberList.indexOf(pn),
                    options = ProductUtils.ProductNumberList
                ) {
                    pn = ProductUtils.ProductNumberList[it]
                    viewModel.setProductNumber(pn)
                }
            }

            SettingsRow(title = stringResource(id = R.string.serial_number)) {
                ArgumentsSetField(
                    modifier = Modifier.width(350.dp),
                    value = sn,
                ) {
                    sn = it
                    viewModel.setSerialNumber(sn)
                }
            }
        }
    }
}

@Composable
fun SettingsArgumentsEquipmentTopBar(
    modifier: Modifier = Modifier,
    navigationActions: NavigationActions
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(brush = zktyBrush, shape = MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .clickable { navigationActions.navigateUp() }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.AutoMirrored.Default.Reply, contentDescription = "Back")
            Text(text = stringResource(id = R.string.equipment_arguments), style = MaterialTheme.typography.titleLarge)
        }
    }
}