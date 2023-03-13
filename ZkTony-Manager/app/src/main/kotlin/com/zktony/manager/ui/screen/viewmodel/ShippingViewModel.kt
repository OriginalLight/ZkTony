package com.zktony.manager.ui.screen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.manager.data.local.model.User
import com.zktony.manager.data.remote.model.*
import com.zktony.manager.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
@HiltViewModel
class ShippingViewModel @Inject constructor(
    private val softWareRepository: SoftwareRepository,
    private val equipmentRepository: EquipmentRepository,
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ShippingUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.getAll().collect {
                if (it.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        user = it[0],
                        product = _uiState.value.product.copy(create_by = it[0].name)
                    )
                }
            }
        }
    }

    fun navigateTo(page: ShippingPageEnum) {
        _uiState.value = _uiState.value.copy(page = page)
    }

    fun setSoftware(software: Software) {
        val product = _uiState.value.product.copy(
            software_id = software.id,
        )
        _uiState.value = _uiState.value.copy(
            software = software,
            product = product
        )
    }

    fun searchCustomer() {
        viewModelScope.launch {
            val value = _uiState.value.searchReq.customer
            if (value.isNotEmpty()) {
                // 判断value是手机号还是姓名
                var searchReq = CustomerQueryDTO()
                searchReq = if (value.matches(Regex("^1[3-9]\\d{9}\$"))) {
                    searchReq.copy(phone = value)
                } else {
                    searchReq.copy(name = value)
                }
                customerRepository.search(searchReq)
                    .flowOn(Dispatchers.IO)
                    .catch {
                        val product = _uiState.value.product.copy(customer_id = "")
                        _uiState.value = _uiState.value.copy(
                            customer = null,
                            product = product
                        )
                    }
                    .collect {
                        val data = it.body()
                        if (data != null && data.isNotEmpty()) {
                            _uiState.value = _uiState.value.copy(
                                customer = data[0],
                                product = _uiState.value.product.copy(customer_id = data[0].id)
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                customer = null,
                                product = _uiState.value.product.copy(customer_id = "")
                            )
                        }
                    }
            }
        }
    }


    fun saveCustomer(customer: Customer, add: Boolean) {
        viewModelScope.launch {
            val result = if (add) {
                customerRepository.add(customer)
            } else {
                customerRepository.update(customer)
            }
            result.flowOn(Dispatchers.IO)
                .collect {
                    _uiState.value = _uiState.value.copy(
                        customer = customer,
                        product = _uiState.value.product.copy(customer_id = customer.id)
                    )
                }
        }
    }

    fun searchEquipment() {
        viewModelScope.launch {
            val value = _uiState.value.searchReq.equipment
            if (value.isNotEmpty()) {
                // 判断value是机器名还是机器型号
                var searchReq = EquipmentQueryDTO()
                // 中文开头是设备名
                searchReq = if (value.matches(Regex("^[\u4e00-\u9fa5].*\$"))) {
                    searchReq.copy(name = value)
                } else {
                    searchReq.copy(model = value)
                }
                equipmentRepository.search(searchReq)
                    .flowOn(Dispatchers.IO)
                    .catch {
                        _uiState.value = _uiState.value.copy(
                            equipment = null,
                            product = _uiState.value.product.copy(
                                equipment_id = "",
                                attachment = ""
                            )
                        )
                    }
                    .collect {
                        val data = it.body()
                        if (data != null && data.isNotEmpty()) {
                            _uiState.value = _uiState.value.copy(
                                equipment = data[0],
                                product = _uiState.value.product.copy(
                                    equipment_id = data[0].id,
                                    attachment = ""
                                )
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                equipment = null,
                                product = _uiState.value.product.copy(
                                    equipment_id = "",
                                    attachment = ""
                                )
                            )
                        }
                    }
            }
        }
    }

    fun saveEquipment(equipment: Equipment, add: Boolean) {
        viewModelScope.launch {
            val result = if (add) {
                equipmentRepository.add(equipment)
            } else {
                equipmentRepository.update(equipment)
            }
            result.flowOn(Dispatchers.IO)
                .collect {
                    _uiState.value = _uiState.value.copy(
                        equipment = equipment,
                        product = _uiState.value.product.copy(
                            equipment_id = equipment.id,
                            attachment = ""
                        )
                    )
                }
        }
    }

    fun searchReqChange(req: SearchReq) {
        _uiState.value = _uiState.value.copy(searchReq = req)
    }

    fun productChange(product: Product) {
        _uiState.value = _uiState.value.copy(product = product)
    }

    fun saveShipping(block: () -> Unit) {
        viewModelScope.launch {
            softWareRepository.add(_uiState.value.software)
                .flowOn(Dispatchers.IO)
                .collect {
                    productRepository.add(_uiState.value.product)
                        .flowOn(Dispatchers.IO)
                        .collect {
                            _uiState.value = ShippingUiState()
                            block()
                        }
                }
        }
    }
}

data class ShippingUiState(
    val user: User? = null,
    val product: Product = Product(),
    val customer: Customer? = null,
    val software: Software = Software(),
    val equipment: Equipment? = null,
    val searchReq: SearchReq = SearchReq(),
    val loading: Boolean = false,
    val error: String = "",
    val page: ShippingPageEnum = ShippingPageEnum.SHIPPING,
)

enum class ShippingPageEnum {
    SHIPPING,
    SOFTWARE_MODIFY,
    CUSTOMER_MODIFY,
    EQUIPMENT_MODIFY,
}

data class SearchReq(
    val customer: String = "",
    val equipment: String = "",
)