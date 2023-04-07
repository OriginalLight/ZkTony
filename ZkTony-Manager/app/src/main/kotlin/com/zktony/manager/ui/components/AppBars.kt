package com.zktony.manager.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zktony.proto.*

/**
 * @author: 刘贺贺
 * @date: 2023-02-17 11:26
 */

// region ManagerAppBar
@SuppressLint("ModifierParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerAppBar(
    modifier: Modifier = Modifier,
    title: String = "Title",
    isFullScreen: Boolean = false,
    onBack: () -> Unit = {},
    actions : @Composable (() -> Unit)? = null,
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.inverseOnSurface
        ),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        navigationIcon = {
            if (isFullScreen) {
                FilledIconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(8.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        },
        actions = {
            actions?.invoke()
        }
    )
}
// endregion

// region OrderSearchBar
@Composable
fun OrderSearchBar(
    modifier: Modifier = Modifier,
    onSearch: (OrderSearch) -> Unit,
) {

    val mSoftWareId = remember { mutableStateOf("") }
    val mInstrumentId = remember { mutableStateOf("") }
    val mExpressNumber = remember { mutableStateOf("") }
    val mTime = remember { mutableStateOf("") }

    Column(
        modifier = modifier
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

// region CustomerSearchBar
@Composable
fun CustomerSearchBar(
    modifier: Modifier = Modifier,
    onSearch: (CustomerSearch) -> Unit,
) {

    val mName = remember { mutableStateOf("") }
    val mPhone = remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        CommonTextField(
            label = "客户名",
            value = mName.value,
            icon = Icons.Filled.Person,
            onValueChange = {
                mName.value = it
            }
        )
        Spacer(modifier = Modifier.height(8.dp))

        CommonTextField(
            label = "手机号",
            value = mPhone.value,
            icon = Icons.Filled.Phone,
            onValueChange = {
                mPhone.value = it
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    onSearch(
                        customerSearch {
                            name = mName.value
                            phone = mPhone.value
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
                    mName.value = ""
                    mPhone.value = ""
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

// region InstrumentSearchBar
@Composable
fun InstrumentSearchBar(
    modifier: Modifier = Modifier,
    onSearch: (InstrumentSearch) -> Unit,
) {

    val mName = remember { mutableStateOf("") }
    val mModel = remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        CommonTextField(
            label = "仪器名称",
            value = mName.value,
            icon = Icons.Filled.TextFields,
            onValueChange = {
                mName.value = it
            }
        )
        Spacer(modifier = Modifier.height(8.dp))

        CommonTextField(
            label = "仪器型号",
            value = mModel.value,
            icon = Icons.Filled.Mode,
            onValueChange = {
                mModel.value = it
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    onSearch(
                        instrumentSearch {
                            name = mName.value
                            model = mModel.value
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
                    mName.value = ""
                    mModel.value = ""
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

// region SoftwareSearchBar
@Composable
fun SoftwareSearchBar(
    modifier: Modifier = Modifier,
    onSearch: (SoftwareSearch) -> Unit,
) {

    val mId = remember { mutableStateOf("") }
    val mPackage = remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        CodeTextField(
            label = "软件编号",
            value = mId.value,
            onValueChange = {
                mId.value = it
            },
            onSoftwareChange = {
                mId.value = it.id
            },
        )
        Spacer(modifier = Modifier.height(8.dp))
        CommonTextField(
            label = "软件包名",
            value = mPackage.value,
            icon = Icons.Filled.Mode,
            onValueChange = {
                mPackage.value = it
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    onSearch(
                        softwareSearch {
                            id = mId.value
                            package_ = mPackage.value
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
                    mId.value = ""
                    mPackage.value = ""
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
