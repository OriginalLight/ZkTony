package com.zktony.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.manager.ext.showShortToast
import com.zktony.manager.data.local.dao.UserDao
import com.zktony.manager.data.local.entity.User
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
class OrderViewModel constructor(
    private val orderGrpc: OrderGrpc,
    private val softwareGrpc: SoftwareGrpc,
    private val customerGrpc: CustomerGrpc,
    private val instrumentGrpc: InstrumentGrpc,
    private val dao: UserDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getAll().collect {
                if (it.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        user = it[0],
                    )
                }
            }
        }
    }

    fun initSoftware(id: String) {
        viewModelScope.launch {
            softwareGrpc.getById(id).flowOn(Dispatchers.IO)
                .catch { it.message.toString().showShortToast() }
                .collect {
                    _uiState.value = _uiState.value.copy(
                        software = it,
                    )
                }
        }
    }

    fun initCustomer(customer: Customer) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                customer = customer,
            )
        }
    }

    fun initInstrument(instrument: Instrument) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                instrument = instrument,
            )
        }
    }

    fun loadCustomerList() {
        viewModelScope.launch {
            customerGrpc.getCustomerPage(
                customerRequestPage {
                    page = 1
                    pageSize = 20
                }
            ).catch { it.message.toString().showShortToast() }
                .collect {
                    _uiState.value = _uiState.value.copy(customerList = it.listList)
                }
        }
    }

    fun searchCustomer(search: CustomerSearch) {
        viewModelScope.launch {
            customerGrpc.searchCustomer(search).catch {
                "查询失败".showShortToast()
            }.collect {
                _uiState.value = _uiState.value.copy(customerList = it.listList)
            }
        }
    }


    fun loadInstrumentList() {
        viewModelScope.launch {
            instrumentGrpc.getInstrumentPage(
                instrumentRequestPage {
                    page = 1
                    pageSize = 20
                }
            ).catch { it.message.toString().showShortToast() }
                .collect {
                    _uiState.value = _uiState.value.copy(instrumentList = it.listList)
                }
        }
    }

    fun searchInstrument(search: InstrumentSearch) {
        viewModelScope.launch {
            instrumentGrpc.searchInstrument(search).catch {
                "查询失败".showShortToast()
            }.collect {
                _uiState.value = _uiState.value.copy(instrumentList = it.listList)
            }
        }
    }

    fun addOrder(order: Order, block: () -> Unit) {
        viewModelScope.launch {
            orderGrpc.add(order).flowOn(Dispatchers.IO)
                .catch {
                    "保存发货信息失败".showShortToast()
                }
                .collect {
                    _uiState.value = OrderUiState()
                    block()
                }
        }
    }
}

data class OrderUiState(
    val user: User? = null,
    val customerList: List<Customer> = emptyList(),
    val customer: Customer? = null,
    val software: Software? = null,
    val instrumentList: List<Instrument> = emptyList(),
    val instrument: Instrument? = null,
)
