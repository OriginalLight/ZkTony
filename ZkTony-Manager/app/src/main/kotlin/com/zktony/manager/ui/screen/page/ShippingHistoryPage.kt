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
import com.zktony.manager.ui.components.CodeTextField
import com.zktony.manager.ui.components.ManagerAppBar
import com.zktony.manager.ui.components.ProductCard
import com.zktony.manager.ui.components.TimeTextField
import com.zktony.manager.ui.screen.viewmodel.ShippingHistoryPageEnum
import com.zktony.manager.ui.screen.viewmodel.ShippingHistoryUiState
import com.zktony.proto.Order
import com.zktony.proto.OrderSearch
import com.zktony.proto.orderSearch

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
    onSearch: (OrderSearch) -> Unit = {},
    onProductClick: (Order) -> Unit = {},
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
            ProductSearchBar(
                modifier = Modifier,
                onSearch = {
                    onSearch(it)
                    isSearchExpanded.value = !isSearchExpanded.value
                },
            )
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
                uiState.orderList.forEach {
                    item {
                        ProductCard(modifier = Modifier,
                            order = it,
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
    onSearch: (OrderSearch) -> Unit,
) {

    val mSoftWareId = remember { mutableStateOf("") }
    val mInstrumentId = remember { mutableStateOf("") }
    val mExpressNumber = remember { mutableStateOf("") }
    val mTime = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        CodeTextField(
            label = "软件编号",
            value = mSoftWareId.value,
            onValueChange = {
                mSoftWareId.value = it
            },
            onSoftwareChange = {
                mSoftWareId.value = it.id
            },
        )
        Spacer(modifier = Modifier.height(8.dp))

        CodeTextField(
            label = "设备编号",
            value = mInstrumentId.value,
            onValueChange = {
                mInstrumentId.value = it
            },
            isQrCode = false,
        )

        Spacer(modifier = Modifier.height(8.dp))

        CodeTextField(
            label = "快递编号",
            value = mExpressNumber.value,
            onValueChange = {
                mExpressNumber.value = it
            },
            isQrCode = false,
        )

        Spacer(modifier = Modifier.height(8.dp))

        TimeTextField(
            label = "生产日期",
            value = mTime.value,
            onValueChange = {
                mTime.value = it
            })

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    onSearch(
                        orderSearch {
                            softwareId = mSoftWareId.value
                            instrumentId = mInstrumentId.value
                            expressNumber = mExpressNumber.value
                            beginTime = mTime.value
                            endTime = mTime.value
                        }
                    )
                },
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
                onClick = {
                    mSoftWareId.value = ""
                    mInstrumentId.value = ""
                    mExpressNumber.value = ""
                    mTime.value = ""
                },
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
        onSearch = {},
    )
}
// endregion