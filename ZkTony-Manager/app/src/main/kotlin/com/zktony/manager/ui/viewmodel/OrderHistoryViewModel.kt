package com.zktony.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.manager.common.ext.showShortToast
import com.zktony.manager.data.remote.grpc.CustomerGrpc
import com.zktony.manager.data.remote.grpc.InstrumentGrpc
import com.zktony.manager.data.remote.grpc.OrderGrpc
import com.zktony.manager.data.remote.grpc.SoftwareGrpc
import com.zktony.proto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
class OrderHistoryViewModel constructor(
    private val orderGrpc: OrderGrpc,
    private val softwareGrpc: SoftwareGrpc,
    private val customerGrpc: CustomerGrpc,
    private val instrumentGrpc: InstrumentGrpc,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OrderHistoryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            orderGrpc.getOrderPage(orderRequestPage {
                page = 1
                pageSize = 40
            }).flowOn(Dispatchers.IO)
                .catch { it.message.toString().showShortToast() }
                .collect {
                    _uiState.value = _uiState.value.copy(orderList = it.listList)
                }
        }
    }


    fun search(search: OrderSearch) {
        viewModelScope.launch {
            orderGrpc.searchOrder(search).flowOn(Dispatchers.IO)
                .catch {
                    "查询失败".showShortToast()
                }
                .collect {
                    _uiState.value = _uiState.value.copy(orderList = it.listList)
                }
        }
    }

    fun clickOrder(order: Order) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(order = order)
            launch {
                if (order.softwareId.isNotEmpty()) {
                    softwareGrpc.getById(order.softwareId).flowOn(Dispatchers.IO)
                        .catch {
                            _uiState.value = _uiState.value.copy(software = null)
                        }
                        .collect {
                            _uiState.value = _uiState.value.copy(software = it)
                        }
                }
            }
            launch {
                if (order.instrumentId.isNotEmpty()) {
                    instrumentGrpc.getById(order.instrumentId).flowOn(Dispatchers.IO)
                        .catch {
                            _uiState.value = _uiState.value.copy(instrument = null)
                        }
                        .collect {
                            _uiState.value = _uiState.value.copy(instrument = it)
                        }
                }
            }
            launch {
                if (order.customerId.isNotEmpty()) {
                    customerGrpc.getById(order.customerId).flowOn(Dispatchers.IO)
                        .catch {
                            _uiState.value = _uiState.value.copy(customer = null)
                        }
                        .collect {
                            _uiState.value = _uiState.value.copy(customer = it)
                        }
                }
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            _uiState.value.order?.let { order ->
                orderGrpc.delete(order.id).flowOn(Dispatchers.IO)
                    .catch {
                        "删除失败".showShortToast()
                    }
                    .collect {
                        "删除成功".showShortToast()
                        _uiState.value = _uiState.value.copy(
                            orderList = _uiState.value.orderList.filter { o -> o.id != order.id },
                            order = null,
                            software = null,
                            instrument = null,
                            customer = null,
                        )
                    }
            }
        }
    }
}

data class OrderHistoryUiState(
    val orderList: List<Order> = emptyList(),
    val order: Order? = null,
    val software: Software? = null,
    val instrument: Instrument? = null,
    val customer: Customer? = null,
)
