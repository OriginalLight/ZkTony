package com.zktony.manager.ui.screen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.manager.common.ext.showShortToast
import com.zktony.manager.data.local.dao.UserDao
import com.zktony.manager.data.local.model.User
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
class ShippingViewModel constructor(
    private val orderGrpc: OrderGrpc,
    private val softwareGrpc: SoftwareGrpc,
    private val customerGrpc: CustomerGrpc,
    private val instrumentGrpc: InstrumentGrpc,
    private val dao: UserDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(ShippingUiState())
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

    fun navigateTo(page: ShippingPageEnum) {
        _uiState.value = _uiState.value.copy(page = page)
    }

    fun setSoftware(software: Software) {
        _uiState.value = _uiState.value.copy(software = software)
    }

    fun searchCustomer(value: String) {
        viewModelScope.launch {
            if (value.isEmpty()) return@launch
            val req = customerSearch {
                if (value.matches(Regex("^1[3-9]\\d{9}\$"))) {
                    phone = value
                } else {
                    name = value
                }
            }
            customerGrpc.searchCustomer(req)
                .flowOn(Dispatchers.IO)
                .catch {
                    _uiState.value = _uiState.value.copy(
                        customer = null,
                    )
                }
                .collect {
                    if (it.listList.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            customer = it.getList(0),
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            customer = null,
                        )
                    }
                }
        }
    }


    fun saveCustomer(customer: Customer, add: Boolean) {
        viewModelScope.launch {
            if (add) {
                customerGrpc.add(customer)
                    .flowOn(Dispatchers.IO)
                    .catch {
                        "保存客户失败".showShortToast()
                    }
                    .collect {
                        _uiState.value = _uiState.value.copy(
                            customer = customer,
                        )
                        "保存客户成功".showShortToast()
                    }
            } else {
                customerGrpc.update(customer)
                    .flowOn(Dispatchers.IO)
                    .catch {
                        "修改客户失败".showShortToast()
                    }
                    .collect {
                        if (it.success) {
                            _uiState.value = _uiState.value.copy(
                                customer = customer,
                            )
                            "修改客户成功".showShortToast()
                        } else {
                            "修改客户失败".showShortToast()
                        }
                    }
            }
        }
    }

    fun searchInstrument(value: String) {
        viewModelScope.launch {
            if (value.isEmpty()) return@launch

            val req = instrumentSearch {
                if (value.matches(Regex("^[\u4e00-\u9fa5].*\$"))) {
                    name = value
                } else {
                    model = value
                }
            }
            instrumentGrpc.searchInstrument(req)
                .flowOn(Dispatchers.IO)
                .catch {
                    _uiState.value = _uiState.value.copy(
                        instrument = null,
                    )
                }
                .collect {
                    if (it.listList.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            instrument = it.getList(0),
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            instrument = null,
                        )
                    }
                }
        }
    }

    fun saveInstrument(instrument: Instrument, add: Boolean) {
        viewModelScope.launch {
            if (add) {
                instrumentGrpc.add(instrument)
                    .flowOn(Dispatchers.IO)
                    .catch {
                        "保存仪器失败".showShortToast()
                    }
                    .collect {
                        _uiState.value = _uiState.value.copy(
                            instrument = instrument,
                        )
                        "保存仪器成功".showShortToast()
                    }
            } else {
                instrumentGrpc.update(instrument)
                    .flowOn(Dispatchers.IO)
                    .catch {
                        "修改仪器失败".showShortToast()
                    }
                    .collect {
                        if (it.success) {
                            _uiState.value = _uiState.value.copy(
                                instrument = instrument,
                            )
                            "修改仪器成功".showShortToast()
                        } else {
                            "修改仪器失败".showShortToast()
                        }
                    }
            }
        }
    }

    fun saveShipping(order: Order, block: () -> Unit) {
        viewModelScope.launch {
            _uiState.value.software?.let { software ->
                softwareGrpc.add(software).flowOn(Dispatchers.IO)
                    .catch {
                        "保存软件信息失败".showShortToast()
                    }
                    .collect {
                        orderGrpc.add(order).flowOn(Dispatchers.IO)
                            .catch {
                                "保存发货信息失败".showShortToast()
                            }
                            .collect {
                                _uiState.value = ShippingUiState()
                                block()
                            }
                    }
            }
        }
    }
}

data class ShippingUiState(
    val user: User? = null,
    val customer: Customer? = null,
    val software: Software? = null,
    val instrument: Instrument? = null,
    val loading: Boolean = false,
    val error: String = "",
    val page: ShippingPageEnum = ShippingPageEnum.SHIPPING,
)

enum class ShippingPageEnum {
    SHIPPING,
    SOFTWARE_MODIFY,
    CUSTOMER_MODIFY,
    INSTRUMENT_MODIFY,
}