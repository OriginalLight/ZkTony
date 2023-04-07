package com.zktony.manager.ui.fragment

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.LaptopMac
import androidx.compose.material.icons.outlined.Note
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.manager.R
import com.zktony.manager.ui.components.*
import com.zktony.manager.ui.viewmodel.HomePageEnum
import com.zktony.manager.ui.viewmodel.OrderViewModel
import com.zktony.proto.order
import com.zktony.proto.software
import com.zktony.www.common.extension.currentTime
import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2023-02-23 13:13
 */

@Composable
fun OrderFragment(
    navigateTo: (HomePageEnum) -> Unit,
    viewModel: OrderViewModel,
    isDualPane: Boolean = false
) {
    BackHandler {
        navigateTo(HomePageEnum.HOME)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val mSoftwareId = remember { mutableStateOf("") }
    val mCustomerReq = remember { mutableStateOf("") }
    val mInstrumentReq = remember { mutableStateOf("") }
    val mInstrumentNumber = remember { mutableStateOf("") }
    val mInstrumentTime = remember { mutableStateOf("") }
    val mAttachment = remember { mutableStateOf("") }
    val mExpressNumber = remember { mutableStateOf("") }
    val mExpressCompany = remember { mutableStateOf("") }
    val mRemarks = remember { mutableStateOf("") }


    Column {
        ManagerAppBar(
            title = stringResource(id = R.string.page_shipping_title),
            isFullScreen = !isDualPane,
            onBack = { navigateTo(HomePageEnum.HOME) },
        )

        val lazyListState = rememberLazyListState()

        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .animateContentSize()
                .fillMaxSize(),
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                CodeTextField(
                    label = "软件编号",
                    value = mSoftwareId.value,
                    onValueChange = {
                        mSoftwareId.value = it
                        if (mSoftwareId.value.isNotEmpty()) {
                            viewModel.setSoftware(
                                software {
                                    id = it
                                    package_ = "com.zktony.not.found"
                                    versionName = "未找到"
                                    versionCode = 0
                                    buildType = "未找到"
                                    remarks = "未找到"
                                    createTime = currentTime()
                                }
                            )
                        } else {
                            viewModel.setSoftware(null)
                        }
                    },
                    onSoftwareChange = { viewModel.setSoftware(it) },
                )
            }
            if (uiState.software != null) {
                item {
                    val s = uiState.software!!
                    val textList = listOf(
                        "软件编号" to s.id,
                        "软件包名" to s.`package`,
                        "软件版本" to s.versionName,
                        "软件代号" to s.versionCode.toString(),
                        "构建类型" to s.buildType,
                        "备注说明" to s.remarks.ifEmpty { "无" },
                    )
                    TextCard(
                        textList = textList,
                        onClick = { }
                    )
                }
            }
            item {
                SearchTextField(
                    label = "客户姓名/手机",
                    value = mCustomerReq.value,
                    onValueChange = { mCustomerReq.value = it },
                    icon = Icons.Outlined.Person,
                    onSearch = { viewModel.searchCustomer(it) },
                )
            }
            if (uiState.customer != null) {
                item {
                    val c = uiState.customer!!
                    val textList = listOf(
                        "客户编号" to c.id,
                        "客户姓名" to c.name,
                        "客户手机" to c.phone,
                        "客户地址" to c.address,
                        "信息来源" to c.source,
                        "从事行业" to c.industry,
                        "客户备注" to c.remarks.ifEmpty { "无" },
                    )
                    TextCard(
                        textList = textList,
                        onClick = { }
                    )
                }
            }

            item {
                SearchTextField(
                    label = "机器名称/型号",
                    value = mInstrumentReq.value,
                    onValueChange = { mInstrumentReq.value = it },
                    icon = Icons.Outlined.LaptopMac,
                    onSearch = { viewModel.searchInstrument(it) },
                )
            }

            if (uiState.instrument != null) {
               item {
                   val e = uiState.instrument!!
                   val textList = listOf(
                       "机器编号" to e.id,
                       "机器名称" to e.name,
                       "机器型号" to e.model,
                       "使用电压" to e.voltage,
                       "使用功率" to e.power,
                       "使用频率" to e.frequency,
                       "机器附件" to e.attachment,
                       "机器备注" to e.remarks.ifEmpty { "无" },
                   )
                   TextCard(
                       textList = textList,
                       onClick = { }
                   )
               }
            }

            item {
                CodeTextField(
                    label = "设备编号",
                    value = mInstrumentNumber.value,
                    onValueChange = { mInstrumentNumber.value = it },
                    isQrCode = false,
                )
            }
            item {
                TimeTextField(
                    label = "生产日期",
                    value = mInstrumentTime.value,
                    onValueChange = { mInstrumentTime.value = it },
                )
            }
            if (uiState.instrument != null && uiState.instrument!!.attachment.isNotEmpty()) {
                item {
                    AttachmentCard(
                        attachment = uiState.instrument!!.attachment,
                        value = uiState.instrument!!.attachment,
                        onValueChange = { mAttachment.value = it }
                    )
                }
            }
            item {
                CodeTextField(
                    label = "快递编号",
                    value = mExpressNumber.value,
                    onValueChange = { mExpressNumber.value = it },
                    isQrCode = false,
                )
            }
            item {
                CommonTextField(
                    label = "快递公司",
                    value = mExpressCompany.value,
                    icon = Icons.Outlined.Domain,
                    onValueChange = { mExpressCompany.value = it },
                )
            }
            item {
                CommonTextField(
                    label = "备注说明",
                    value = mRemarks.value,
                    icon = Icons.Outlined.Note,
                    singleLine = false,
                    onValueChange = { mRemarks.value = it },
                )
            }
            item {
                AnimatedVisibility(
                    visible = mSoftwareId.value.isNotEmpty() &&
                            mInstrumentNumber.value.isNotEmpty() &&
                            mExpressNumber.value.isNotEmpty() &&
                            uiState.customer != null &&
                            uiState.instrument != null
                ) {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth(),
                        onClick = {
                            viewModel.addOrder(
                                order = order {
                                    id = UUID.randomUUID().toString()
                                    softwareId = mSoftwareId.value
                                    customerId = uiState.customer?.id ?: ""
                                    instrumentId = uiState.instrument?.id ?: ""
                                    instrumentNumber = mInstrumentNumber.value
                                    instrumentTime = mInstrumentTime.value + " 00:00:00"
                                    attachment = mAttachment.value
                                    expressNumber = mExpressNumber.value
                                    expressCompany = mExpressCompany.value
                                    remarks = mRemarks.value
                                    createTime = currentTime()
                                },
                                block = { navigateTo(HomePageEnum.ORDER_HISTORY) }
                            )
                        },
                    ) {
                        Text(text = "保存")
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

}
