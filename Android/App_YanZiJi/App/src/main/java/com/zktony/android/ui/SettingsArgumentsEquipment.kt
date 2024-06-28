package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zktony.android.R
import com.zktony.android.ui.components.ArgumentsInputGroup
import com.zktony.android.ui.components.DropDownBox
import com.zktony.android.ui.components.TitleBackTopBar
import com.zktony.android.ui.utils.LocalNavigationActions
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TitleBackTopBar(stringResource(id = R.string.equipment_arguments))

        SettingsRow(title = stringResource(id = R.string.pruduct_number)) {
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
            ArgumentsInputGroup(
                modifier = Modifier.width(350.dp),
                value = sn
            ) {
                sn = it
                viewModel.setSerialNumber(sn)
            }
        }
    }
}