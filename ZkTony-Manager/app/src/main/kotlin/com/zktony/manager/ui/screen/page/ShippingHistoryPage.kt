package com.zktony.manager.ui.screen.page

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zktony.manager.R
import com.zktony.manager.data.remote.model.Product
import com.zktony.manager.data.remote.model.ProductQueryDTO
import com.zktony.manager.ui.components.CodeTextField
import com.zktony.manager.ui.components.ManagerAppBar
import com.zktony.manager.ui.components.ProductCard
import com.zktony.manager.ui.components.TimeTextField
import com.zktony.manager.ui.screen.viewmodel.ShippingHistoryPageEnum
import com.zktony.manager.ui.screen.viewmodel.ShippingHistoryUiState

/**
 * @author: 刘贺贺
 * @date: 2023-02-23 13:13
 */

// region ShippingPage
@Composable
fun ShippingHistoryPage(
    modifier: Modifier = Modifier,
    uiState: ShippingHistoryUiState,
    navigateTo: (ShippingHistoryPageEnum) -> Unit,
    isDualPane: Boolean = false,
    onSearch: () -> Unit = {},
    onProductClick: (Product) -> Unit = {},
    onQueryDtoChange: (ProductQueryDTO) -> Unit = {},
    onBack: () -> Unit,
) {
    BackHandler {
        onBack()
    }

    Column {
        val isSearchExpanded = remember { mutableStateOf(false) }

        ManagerAppBar(title = stringResource(id = R.string.page_shipping_history_title),
            isFullScreen = !isDualPane,
            onBack = onBack,
            actions = {
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
            })

        // 搜索栏
        AnimatedVisibility(visible = isSearchExpanded.value) {
            ProductSearchBar(modifier = Modifier,
                onSearch = {
                    onSearch()
                    isSearchExpanded.value = !isSearchExpanded.value
                },
                queryDTO = uiState.queryDTO,
                onValueChange = { onQueryDtoChange(it) })
        }

        // 列表状态
        val listState = rememberLazyListState()
        LazyColumn(modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = listState,
            content = {
                uiState.productList.forEach {
                    item {
                        ProductCard(modifier = Modifier,
                            product = it,
                            onClick = { onProductClick(it) })
                    }
                }
            })
    }

}
// endregion

// region ProductSearchBar
@Composable
fun ProductSearchBar(
    modifier: Modifier = Modifier,
    onSearch: () -> Unit,
    queryDTO: ProductQueryDTO,
    onValueChange: (ProductQueryDTO) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        CodeTextField(
            label = "软件编号",
            value = queryDTO.software_id,
            onValueChange = {
                onValueChange(queryDTO.copy(software_id = it))
            },
            onSoftwareChange = {
                onValueChange(queryDTO.copy(software_id = it.id))
            },
        )
        Spacer(modifier = Modifier.height(8.dp))

        CodeTextField(
            label = "设备编号",
            value = queryDTO.equipment_number,
            onValueChange = {
                onValueChange(queryDTO.copy(equipment_number = it))
            },
            isQrCode = false,
        )

        Spacer(modifier = Modifier.height(8.dp))

        CodeTextField(
            label = "快递编号",
            value = queryDTO.express_number,
            onValueChange = {
                onValueChange(queryDTO.copy(express_number = it))
            },
            isQrCode = false,
        )

        Spacer(modifier = Modifier.height(8.dp))

        TimeTextField(
            label = "生产日期",
            value = queryDTO.begin_time,
            onValueChange = {
                onValueChange(queryDTO.copy(begin_time = it, end_time = it))
            })

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = { onSearch() },
            ) {
                Row {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(text = "搜索")
                }
            }
            Button(
                modifier = Modifier.weight(1f),
                onClick = { onValueChange(ProductQueryDTO()) },
            ) {
                Row {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(text = "清空")
                }
            }
        }
    }
}
// endregion

// region Preview
@Preview
@Composable
fun ShippingHistoryPagePreview() {
    ShippingHistoryPage(uiState = ShippingHistoryUiState(), navigateTo = {}, onBack = {})
}

@Preview
@Composable
fun ProductSearchBarPreview() {
    ProductSearchBar(
        queryDTO = ProductQueryDTO(),
        onValueChange = {},
        onSearch = {},
    )
}
// endregion