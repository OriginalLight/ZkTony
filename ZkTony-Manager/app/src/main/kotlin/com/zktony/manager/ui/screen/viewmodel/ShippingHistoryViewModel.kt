package com.zktony.manager.ui.screen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class ShippingHistoryViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val softwareRepository: SoftwareRepository,
    private val equipmentRepository: EquipmentRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ShippingHistoryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            productRepository.search(_uiState.value.queryDTO)
                .flowOn(Dispatchers.IO)
                .catch { _uiState.value = _uiState.value.copy(error = it.message ?: "") }
                .collect {
                    val data = it.body()
                    if (data != null) {
                        _uiState.value = _uiState.value.copy(productList = data)
                    } else {
                        _uiState.value = _uiState.value.copy(productList = emptyList())
                    }
                }
        }
    }

    fun navigateTo(page: ShippingHistoryPageEnum) {
        _uiState.value = _uiState.value.copy(page = page)
    }

    fun search() {
        viewModelScope.launch {
            productRepository.search(_uiState.value.queryDTO)
                .flowOn(Dispatchers.IO)
                .catch { _uiState.value = _uiState.value.copy(error = it.message ?: "") }
                .collect {
                    it.body()?.let { productList ->
                        _uiState.value = _uiState.value.copy(productList = productList)
                    }
                }
        }
    }

    fun queryDtoChange(queryDTO: ProductQueryDTO) {
        _uiState.value = _uiState.value.copy(queryDTO = queryDTO)
    }

    fun productClick(product: Product) {
        loadInfo(product)
        _uiState.value = _uiState.value.copy(
            product = product,
            page = ShippingHistoryPageEnum.PRODUCT_DETAIL
        )
    }

    private fun loadInfo(product: Product) {
        viewModelScope.launch {
            launch {
                softwareRepository.search(SoftwareQueryDTO(id = product.software_id))
                    .flowOn(Dispatchers.IO)
                    .catch { _uiState.value = _uiState.value.copy(error = it.message ?: "") }
                    .collect {
                        val data = it.body()
                        if (data != null) {
                            _uiState.value = _uiState.value.copy(software = data[0])
                        } else {
                            _uiState.value = _uiState.value.copy(software = null)
                        }
                    }
            }
            launch {
                equipmentRepository.search(EquipmentQueryDTO(id = product.equipment_id))
                    .flowOn(Dispatchers.IO)
                    .catch { _uiState.value = _uiState.value.copy(error = it.message ?: "") }
                    .collect {
                        val data = it.body()
                        if (data != null) {
                            _uiState.value = _uiState.value.copy(equipment = data[0])
                        } else {
                            _uiState.value = _uiState.value.copy(equipment = null)
                        }
                    }
            }
            launch {
                customerRepository.search(CustomerQueryDTO(id = product.customer_id))
                    .flowOn(Dispatchers.IO)
                    .catch { _uiState.value = _uiState.value.copy(error = it.message ?: "") }
                    .collect {
                        val data = it.body()
                        if (data != null) {
                            _uiState.value = _uiState.value.copy(customer = data[0])
                        } else {
                            _uiState.value = _uiState.value.copy(customer = null)
                        }
                    }
            }
        }
    }
}

data class ShippingHistoryUiState(
    val productList: List<Product> = emptyList(),
    val product: Product? = null,
    val software: Software? = null,
    val equipment: Equipment? = null,
    val customer: Customer? = null,
    val loading: Boolean = false,
    val error: String = "",
    val page: ShippingHistoryPageEnum = ShippingHistoryPageEnum.SHIPPING_HISTORY,
    val queryDTO: ProductQueryDTO = ProductQueryDTO(),
)

enum class ShippingHistoryPageEnum {
    SHIPPING_HISTORY,
    PRODUCT_DETAIL,
}