package com.zktony.manager.ui.fragment

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.manager.ui.components.InstrumentCard
import com.zktony.manager.ui.components.InstrumentSearchBar
import com.zktony.manager.ui.components.ManagerAppBar
import com.zktony.manager.ui.viewmodel.HomePageEnum
import com.zktony.manager.ui.viewmodel.InstrumentViewModel
import com.zktony.manager.ui.viewmodel.ManagerPageEnum
import com.zktony.manager.ui.viewmodel.OrderViewModel

@Composable
fun InstrumentSelectFragment(
    modifier: Modifier = Modifier,
    navigateTo: (HomePageEnum) -> Unit,
    viewModel: OrderViewModel,
    isDualPane: Boolean = false
) {

    BackHandler {
        navigateTo(HomePageEnum.ORDER)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
    ) {
        val isSearchExpanded = remember { mutableStateOf(false) }

        ManagerAppBar(
            title = "仪器列表",
            isFullScreen = !isDualPane,
            onBack = { navigateTo(HomePageEnum.ORDER) },
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
            InstrumentSearchBar(
                modifier = Modifier,
                onSearch = {
                    viewModel.searchInstrument(it)
                    isSearchExpanded.value = !isSearchExpanded.value
                },
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyVerticalGrid(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                columns = GridCells.Adaptive(minSize = 128.dp)
            ) {
                uiState.instrumentList.forEach {
                    item {
                        InstrumentCard(
                            instrument = it,
                            onClick = {
                                viewModel.initInstrument(it)
                                navigateTo(HomePageEnum.ORDER)
                            }
                        )
                    }
                }
            }
        }
    }
}