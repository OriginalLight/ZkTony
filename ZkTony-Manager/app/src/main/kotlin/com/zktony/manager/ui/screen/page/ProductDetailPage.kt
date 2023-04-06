package com.zktony.manager.ui.screen.page

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zktony.manager.ui.components.ManagerAppBar
import com.zktony.manager.ui.components.TextCard
import com.zktony.proto.*
import java.util.*

@Composable
fun ProductDetailPage(
    modifier: Modifier = Modifier,
    order: Order?,
    software: Software?,
    instrument: Instrument?,
    customer: Customer?,
    onBack: () -> Unit,
) {

    BackHandler {
        onBack()
    }

    Column {
        ManagerAppBar(
            title = "产品详情",
            onBack = onBack,
            isFullScreen = true,
        )

        val listState = rememberLazyListState()

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (order != null) {
                item {
                    val textList = listOf(
                        "记录编号" to order.id,
                        "设备编号" to order.instrumentNumber,
                        "生产日期" to order.instrumentTime.replace("T", " "),
                        "快递单号" to order.expressNumber,
                        "快递公司" to order.expressCompany,
                        "发货时间" to order.createTime.replace("T", " "),
                        "备注说明" to order.remarks.ifEmpty { "无" },
                    )
                    TextCard(textList = textList)
                }
            }

            if (software != null) {
                item {
                    TextCard(textList = listOf(
                        "软件编号" to software.id,
                        "软件包名" to software.`package`,
                        "软件版本" to software.versionName,
                        "软件代号" to software.versionCode.toString(),
                        "构建类型" to software.buildType,
                        "备注说明" to software.remarks.ifEmpty { "无" },
                    ))
                }
            }

            if (instrument != null) {
                item {
                    TextCard(
                        textList = listOf(
                            "机器编号" to instrument.id,
                            "机器名称" to instrument.name,
                            "机器型号" to instrument.model,
                            "使用电压" to instrument.voltage,
                            "使用功率" to instrument.power,
                            "使用频率" to instrument.frequency,
                            "机器附件" to instrument.attachment,
                            "机器备注" to instrument.remarks.ifEmpty { "无" },
                        )
                    )
                }
            }

            if(customer != null) {
                item {
                    TextCard(textList = listOf(
                        "客户编号" to customer.id,
                        "客户姓名" to customer.name,
                        "客户手机" to customer.phone,
                        "客户地址" to customer.address,
                        "信息来源" to customer.source,
                        "从事行业" to customer.industry,
                        "客户备注" to customer.remarks.ifEmpty { "无" },
                    ))
                }
            }
        }
    }
}

// region Preview

@Preview
@Composable
fun ProductDetailPagePreview() {
    ProductDetailPage(
        order = order {
            id = UUID.randomUUID().toString()
        },
        software = software {
            id = UUID.randomUUID().toString()
        },
        instrument = instrument {
            id = UUID.randomUUID().toString()
        },
        customer = customer {
            id = UUID.randomUUID().toString()
        },
        onBack = { },
    )
}

// endregion