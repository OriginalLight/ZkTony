package com.zktony.manager.ui.fragment

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.MiscellaneousServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.Note
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.gson.Gson
import com.zktony.manager.R
import com.zktony.manager.common.ext.showShortToast
import com.zktony.manager.data.remote.model.QrCode
import com.zktony.manager.ui.QrCodeActivity
import com.zktony.manager.ui.components.*
import com.zktony.manager.ui.viewmodel.HomePageEnum
import com.zktony.manager.ui.viewmodel.OrderViewModel
import com.zktony.proto.order
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

    val context = LocalContext.current
    val qrCodeScanner =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val result = it.data?.getStringExtra("SCAN_RESULT")
                try {
                    val qrCode = Gson().fromJson(result, QrCode::class.java)
                    viewModel.initSoftware(qrCode.id)
                } catch (e: Exception) {
                    "二维码格式错误".showShortToast()
                }
            }
        }

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
        Spacer(modifier = Modifier.height(16.dp))

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
                Row {
                    IconCard(
                        modifier = Modifier
                            .height(48.dp)
                            .weight(1f),
                        icon = Icons.Filled.Android,
                        color = if (uiState.software != null) {
                            Color.Green
                        } else {
                            Color.Red
                        },
                        onClick = {
                            qrCodeScanner.launch(
                                Intent(
                                    context, QrCodeActivity::class.java
                                )
                            )
                        }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    IconCard(
                        modifier = Modifier
                            .height(48.dp)
                            .weight(1f),
                        icon = Icons.Filled.MiscellaneousServices,
                        color = if (uiState.instrument != null) {
                            Color.Green
                        } else {
                            Color.Red
                        },
                        onClick = {
                            viewModel.loadInstrumentList()
                            navigateTo(HomePageEnum.INSTRUMENT_SELECT)
                        }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    IconCard(
                        modifier = Modifier
                            .height(48.dp)
                            .weight(1f),
                        icon = Icons.Filled.Person,
                        color = if (uiState.customer != null) {
                            Color.Green
                        } else {
                            Color.Red
                        },
                        onClick = {
                            viewModel.loadCustomerList()
                            navigateTo(HomePageEnum.CUSTOMER_SELECT)
                        }
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
                        value = mAttachment.value,
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
                    visible = mInstrumentNumber.value.isNotEmpty() &&
                            mExpressNumber.value.isNotEmpty() &&
                            mInstrumentTime.value.isNotEmpty()
                ) {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth(),
                        onClick = {
                            viewModel.addOrder(
                                order = order {
                                    id = UUID.randomUUID().toString()
                                    softwareId = uiState.software?.id ?: "no software"
                                    customerId = uiState.customer?.id ?: "no customer"
                                    instrumentId = uiState.instrument?.id ?: "no instrument"
                                    instrumentNumber = mInstrumentNumber.value
                                    instrumentTime = mInstrumentTime.value + " 00:00:00"
                                    attachment = mAttachment.value.ifEmpty { "无" }
                                    expressNumber = mExpressNumber.value
                                    expressCompany = mExpressCompany.value.ifEmpty { "无" }
                                    remarks = mRemarks.value.ifEmpty { "无" }
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
