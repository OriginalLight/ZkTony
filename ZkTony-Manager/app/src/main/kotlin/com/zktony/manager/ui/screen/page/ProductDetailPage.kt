package com.zktony.manager.ui.screen.page

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import com.zktony.manager.data.remote.model.Customer
import com.zktony.manager.data.remote.model.Equipment
import com.zktony.manager.data.remote.model.Product
import com.zktony.manager.data.remote.model.Software
import com.zktony.manager.ui.components.ManagerAppBar
import com.zktony.manager.ui.components.TextCard

@Composable
fun ProductDetailPage(
    modifier: Modifier = Modifier,
    product: Product?,
    software: Software?,
    equipment: Equipment?,
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
            if (product != null) {
                item {
                    val textList = listOf(
                        "记录编号" to product.id,
                        "设备编号" to product.equipment_number,
                        "生产日期" to product.equipment_time.replace("T", " "),
                        "快递单号" to product.express_number,
                        "快递公司" to product.express_company,
                        "发货时间" to product.create_time.replace("T", " "),
                        "备注说明" to product.remarks.ifEmpty { "无" },
                    )
                    TextCard(textList = textList)
                }
            }

            if (software != null) {
                item {
                    TextCard(textList = listOf(
                        "软件编号" to software.id,
                        "软件包名" to software.`package`,
                        "软件版本" to software.version_name,
                        "软件代号" to software.version_code.toString(),
                        "构建类型" to software.build_type,
                        "备注说明" to software.remarks.ifEmpty { "无" },
                    ))
                }
            }

            if (equipment != null) {
                item {
                    TextCard(textList = listOf(
                        "机器编号" to equipment.id,
                        "机器名称" to equipment.name,
                        "机器型号" to equipment.model,
                        "使用电压" to equipment.voltage,
                        "使用功率" to equipment.power,
                        "使用频率" to equipment.frequency,
                        "机器附件" to equipment.attachment,
                        "机器备注" to equipment.remarks.ifEmpty { "无" },
                    ))
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
        product = Product(
            id = "1",
            equipment_number = "1",
            equipment_time = "2021-01-01T00:00:00",
            express_number = "1",
            express_company = "1",
            create_time = "2021-01-01T00:00:00",
            software_id = "1",
            equipment_id = "1",
            customer_id = "1",
        ),
        software = Software(
            id = "1",
            `package` = "1",
            version_name = "1",
            version_code = 1,
            build_type = "1",
            remarks = "1",
        ),
        equipment = Equipment(
            id = "1",
            name = "1",
            model = "1",
            voltage = "1",
            power = "1",
            frequency = "1",
            attachment = "1",
            remarks = "1",
        ),
        customer = Customer(
            id = "1",
            name = "1",
            phone = "1",
            address = "1",
            source = "1",
            industry = "1",
            remarks = "1",
        ),
        onBack = { },
    )
}

// endregion