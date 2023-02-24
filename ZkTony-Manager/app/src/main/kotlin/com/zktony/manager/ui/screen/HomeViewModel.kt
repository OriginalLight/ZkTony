package com.zktony.manager.ui.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.manager.data.local.model.User
import com.zktony.manager.data.remote.model.*
import com.zktony.manager.data.remote.result.NetworkResult
import com.zktony.manager.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val softWareRepository: SoftwareRepository,
    private val equipmentRepository: EquipmentRepository,
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()
    private val _shippingUiState = MutableStateFlow(ShippingUiState())
    val shippingUiState = _shippingUiState.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.getAll().collect {
                if (it.isNotEmpty()) {
                    _shippingUiState.value = _shippingUiState.value.copy(
                        user = it[0],
                        product = _shippingUiState.value.product.copy(create_by = it[0].name)
                    )
                }
            }
        }
    }

    fun navigateTo(page: HomePage) {
        _uiState.value = _uiState.value.copy(page = page)
    }

    fun setSoftware(software: Software) {
        _shippingUiState.value = _shippingUiState.value.copy(
            software = software,
            product = _shippingUiState.value.product.copy(software_id = software.id)
        )
    }

    fun searchCustomer() {
        viewModelScope.launch {
            val value = _shippingUiState.value.searchReq.customer
            if (value.isNotEmpty()) {
                // 判断value是手机号还是姓名
                var searchReq = CustomerQueryDTO()
                searchReq = if (value.matches(Regex("^1[3-9]\\d{9}\$"))) {
                    searchReq.copy(phone = value)
                } else {
                    searchReq.copy(name = value)
                }
                customerRepository.search(searchReq).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            if (it.data != null && it.data.isNotEmpty()) {
                                _shippingUiState.value =
                                    _shippingUiState.value.copy(
                                        customer = it.data[0],
                                        product = _shippingUiState.value.product.copy(customer_id = it.data[0].id)
                                    )
                            } else {
                                _shippingUiState.value =
                                    _shippingUiState.value.copy(
                                        customer = null,
                                        product = _shippingUiState.value.product.copy(customer_id = "")
                                    )
                            }
                        }
                        is NetworkResult.Error -> {
                            _shippingUiState.value = _shippingUiState.value.copy(
                                customer = null,
                                product = _shippingUiState.value.product.copy(customer_id = "")
                            )
                        }
                        else -> {}
                    }
                }
            }
        }

    }

    fun addCustomer(customer: Customer) {
        viewModelScope.launch {
            customerRepository.add(customer).collect {
                when (it) {
                    is NetworkResult.Loading -> {
                        _shippingUiState.value = _shippingUiState.value.copy(
                            customer = null,
                            product = _shippingUiState.value.product.copy(customer_id = "")
                        )
                    }
                    is NetworkResult.Success -> {
                        _shippingUiState.value = _shippingUiState.value.copy(
                            customer = customer,
                            product = _shippingUiState.value.product.copy(customer_id = customer.id)
                        )
                    }
                    is NetworkResult.Error -> {
                        _shippingUiState.value = _shippingUiState.value.copy(
                            customer = null,
                            product = _shippingUiState.value.product.copy(customer_id = "")
                        )
                    }
                }
            }
        }
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            customerRepository.update(customer).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        _shippingUiState.value = _shippingUiState.value.copy(customer = customer)
                    }
                    else -> {}
                }
            }
        }
    }

    fun searchEquipment() {
        viewModelScope.launch {
            val value = _shippingUiState.value.searchReq.equipment
            if (value.isNotEmpty()) {
                // 判断value是机器名还是机器型号
                var searchReq = EquipmentQueryDTO()
                // 中文开头是设备名
                searchReq = if (value.matches(Regex("^[\u4e00-\u9fa5].*\$"))) {
                    searchReq.copy(name = value)
                } else {
                    searchReq.copy(model = value)
                }
                equipmentRepository.search(searchReq).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            Log.d("HomeViewModel", "searchEquipment: ${it.data}")
                            if (it.data != null && it.data.isNotEmpty()) {
                                _shippingUiState.value =
                                    _shippingUiState.value.copy(
                                        equipment = it.data[0],
                                        product = _shippingUiState.value.product.copy(
                                            equipment_id = it.data[0].id,
                                            attachment = ""
                                        )
                                    )
                            } else {
                                _shippingUiState.value = _shippingUiState.value.copy(
                                    equipment = null,
                                    product = _shippingUiState.value.product.copy(
                                        attachment = "",
                                        equipment_id = ""
                                    )
                                )
                            }
                        }
                        is NetworkResult.Error -> {
                            Log.e("HomeViewModel", "searchEquipment: $it")
                            _shippingUiState.value = _shippingUiState.value.copy(
                                equipment = null,
                                product = _shippingUiState.value.product.copy(
                                    attachment = "",
                                    equipment_id = ""
                                )
                            )
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    fun addEquipment(equipment: Equipment) {
        viewModelScope.launch {
            equipmentRepository.add(equipment).collect {
                when (it) {
                    is NetworkResult.Loading -> {
                        _shippingUiState.value = _shippingUiState.value.copy(
                            equipment = null,
                            product = _shippingUiState.value.product.copy(
                                equipment_id = "",
                                attachment = ""
                            )
                        )
                    }
                    is NetworkResult.Success -> {
                        _shippingUiState.value = _shippingUiState.value.copy(
                            equipment = equipment,
                            product = _shippingUiState.value.product.copy(
                                equipment_id = equipment.id,
                                attachment = ""
                            )
                        )
                    }
                    is NetworkResult.Error -> {
                        _shippingUiState.value = _shippingUiState.value.copy(
                            equipment = null,
                            product = _shippingUiState.value.product.copy(
                                equipment_id = "",
                                attachment = ""
                            )
                        )
                    }
                }
            }
        }
    }

    fun updateEquipment(equipment: Equipment) {
        viewModelScope.launch {
            equipmentRepository.update(equipment).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        _shippingUiState.value = _shippingUiState.value.copy(equipment = equipment)
                    }
                    else -> {}
                }
            }
        }
    }

    fun searchReqChange(req: SearchReq) {
        _shippingUiState.value = _shippingUiState.value.copy(searchReq = req)
    }

    fun productChange(product: Product) {
        _shippingUiState.value = _shippingUiState.value.copy(product = product)
    }

    fun saveShipping() {
        viewModelScope.launch {
           softWareRepository.add(_shippingUiState.value.software).collect {
               when (it) {
                   is NetworkResult.Success -> {
                       productRepository.add(_shippingUiState.value.product).collect { it1 ->
                           when (it1) {
                               is NetworkResult.Success -> {
                                   _shippingUiState.value = _shippingUiState.value.copy(
                                       product = Product(),
                                       customer = null,
                                       equipment = null,
                                       software = Software(),
                                   )
                                   _uiState.value = _uiState.value.copy(page = HomePage.HOME)
                               }
                               else -> {}
                           }
                       }
                   }
                   else -> {}
               }
           }
        }
    }
}

data class HomeUiState(
    val loading: Boolean = false,
    val error: String = "",
    val page: HomePage = HomePage.HOME,
)

enum class HomePage {
    HOME,
    SHIPPING,
    SHIPPING_HISTORY,
    AFTER_SALE,
    AFTER_SALE_HISTORY,
    SOFTWARE_MODIFY,
    CUSTOMER_MODIFY,
    EQUIPMENT_MODIFY,
}

data class ShippingUiState(
    val user: User? = null,
    val product: Product = Product(),
    val customer: Customer? = null,
    val software: Software = Software(),
    val equipment: Equipment? = null,
    val searchReq: SearchReq = SearchReq(),
)

data class SearchReq(
    val customer: String = "",
    val equipment: String = "",
)