package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zktony.android.R
import com.zktony.android.data.Product
import com.zktony.android.ui.components.ArgumentsInputField
import com.zktony.android.ui.components.BaseTopBar
import com.zktony.android.ui.components.DropDownBox
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.viewmodel.SettingsArgumentsEquipmentViewModel
import com.zktony.android.utils.Constants
import com.zktony.datastore.rememberDataSaverState

@Composable
fun SettingsArgumentsEquipmentView(viewModel: SettingsArgumentsEquipmentViewModel = hiltViewModel()) {

    val navigationActions = LocalNavigationActions.current

    BackHandler {
        // 拦截返回键
        navigationActions.navigateUp()
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 顶部导航栏
        SettingsArgumentsEquipmentTopBar(navigationActions = navigationActions)

        // 参数列表
        EquipmentArgumentsListView(viewModel = viewModel)
    }
}

// 顶部导航栏
@Composable
fun SettingsArgumentsEquipmentTopBar(
    modifier: Modifier = Modifier,
    navigationActions: NavigationActions
) {

    BaseTopBar(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .clickable { navigationActions.navigateUp() }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.Reply,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = stringResource(id = R.string.app_equipment_arguments),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

// 参数列表
@Composable
fun EquipmentArgumentsListView(
    modifier: Modifier = Modifier,
    viewModel: SettingsArgumentsEquipmentViewModel
) {
    // P/N参数
    var pn by rememberDataSaverState(key = Constants.PN, initialValue = Constants.DEFAULT_PN)
    // S/N参数
    var sn by rememberDataSaverState(key = Constants.SN, initialValue = Constants.DEFAULT_SN)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
            .clip(MaterialTheme.shapes.medium),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        SettingsItem(title = stringResource(id = R.string.app_product_number)) {
            DropDownBox(
                modifier = Modifier
                    .width(160.dp)
                    .height(40.dp),
                selected = Product.indexFromName(pn),
                options = Product.getTextList(),
            ) {
                pn = Product.getNameByIndex(it)
                viewModel.setProductNumber(Product.getNameByIndex(it))
            }
        }

        SettingsItem(title = stringResource(id = R.string.app_serial_number)) {
            var snText by remember(sn) { mutableStateOf(sn) }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArgumentsInputField(
                    modifier = Modifier
                        .width(300.dp)
                        .height(48.dp),
                    value = snText,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                ) {
                    snText = it
                }

                Button(
                    onClick = {
                        sn = snText
                        viewModel.setSerialNumber(snText)
                    }
                ) {
                    Text(text = stringResource(id = R.string.app_set))
                }
            }
        }
    }
}