package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zktony.android.R
import com.zktony.android.ui.components.ArgumentsInputGroup
import com.zktony.android.ui.components.RadioButtonGroup
import com.zktony.android.ui.components.SettingsArgumentsTopBar
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.viewmodel.SettingsArgumentsViewModel
import com.zktony.android.utils.Constants
import com.zktony.datastore.rememberDataSaverState

@Composable
fun SettingsArgumentsView(viewModel: SettingsArgumentsViewModel = hiltViewModel()) {

    val navigationActions = LocalNavigationActions.current

    BackHandler {
        // 拦截返回键
        navigationActions.navigateUp()
    }

    Column {
        // 顶部导航栏
        SettingsArgumentsTopBar(viewModel = viewModel)
        // 参数设置
        LazyColumn {
            // 设备参数
            item {
                EquipmentArgumentsView(viewModel = viewModel)
            }
            // 运行参数
            item {
                RuntimeArgumentsView(viewModel = viewModel)
            }
            // 蠕动泵参数
            item {
                PumpArgumentsView(viewModel = viewModel)
            }
            // 电压电流参数
            item {
                VoltageCurrentArgumentsView(viewModel = viewModel)
            }
            // 传感器参数
            item {
                SensorArgumentsView(viewModel = viewModel)
            }
        }
    }
}

// 设备参数
@Composable
fun EquipmentArgumentsView(
    modifier: Modifier = Modifier,
    viewModel: SettingsArgumentsViewModel
) {
    // 设备类型列表
    val pnList = listOf("Y1000", "Y2000", "Y3000", "Y4000")
    // P/N参数
    var pn by rememberDataSaverState(key = Constants.PN, initialValue = "Y1000")
    // S/N参数
    var sn by rememberDataSaverState(key = Constants.SN, initialValue = "Unknown")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.equipment_arguments),
            style = MaterialTheme.typography.headlineMedium
        )

        SettingsArgumentsRaw(title = stringResource(id = R.string.pruduct_number)) {
            RadioButtonGroup(
                selected = pnList.indexOf(pn),
                options = pnList,
            ) {
                pn = pnList[it]
                viewModel.setProductNumber(pnList[it])
            }
        }

        SettingsArgumentsRaw(title = stringResource(id = R.string.serial_number)) {
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

// 运行参数
@Composable
fun RuntimeArgumentsView(
    modifier: Modifier = Modifier,
    viewModel: SettingsArgumentsViewModel
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.runtime_arguments),
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

// 蠕动泵参数
@Composable
fun PumpArgumentsView(
    modifier: Modifier = Modifier,
    viewModel: SettingsArgumentsViewModel
) {

}


// 电压电流参数
@Composable
fun VoltageCurrentArgumentsView(
    modifier: Modifier = Modifier,
    viewModel: SettingsArgumentsViewModel
) {

}

// 传感器参数
@Composable
fun SensorArgumentsView(
    modifier: Modifier = Modifier,
    viewModel: SettingsArgumentsViewModel
) {

}

@Composable
fun SettingsArgumentsRaw(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Normal))
        content()
    }
}

@Preview(device = "spec:parent=Nexus 9")
@Composable
fun EquipmentArgumentsViewPreview() {
    Surface {
        EquipmentArgumentsView(viewModel = hiltViewModel())
    }
}