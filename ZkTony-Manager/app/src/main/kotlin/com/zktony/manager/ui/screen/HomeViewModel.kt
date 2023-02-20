package com.zktony.manager.ui.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.manager.data.remote.model.Customer
import com.zktony.manager.data.remote.model.Equipment
import com.zktony.manager.data.remote.model.Product
import com.zktony.manager.data.remote.model.Software
import com.zktony.manager.data.remote.result.NetworkResult
import com.zktony.manager.data.repository.ApplicationRepository
import com.zktony.manager.data.repository.SoftwareRepository
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
    private val applicationRepository: ApplicationRepository,
    private val softWareRepository: SoftwareRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            applicationRepository.getApplicationById("sss").collect {
                when(it) {
                    is NetworkResult.Success -> { Log.d("HomeViewModel", "init: ${it.data}") }
                    is NetworkResult.Error -> { Log.e("HomeViewModel", "init: ${it.throwable}") }
                    is NetworkResult.Loading -> { }
                }
            }
            softWareRepository.add(Software(
                id = "4"
            )).collect {
                when(it) {
                    is NetworkResult.Success -> { Log.d("HomeViewModel", "add software ${it.data}") }
                    is NetworkResult.Error -> { Log.e("HomeViewModel", "add software ${it.throwable}") }
                    is NetworkResult.Loading -> { }
                }
            }
        }
    }

    fun navigateTo(page: HomePage) {
        _uiState.value = _uiState.value.copy(page = page)
    }

    fun setSoftware(software: Software) {
        _uiState.value = _uiState.value.copy(
            shipping = _uiState.value.shipping.copy(software = software)
        )
    }
}

data class HomeUiState(
    val loading: Boolean = false,
    val error: String = "",
    val page: HomePage = HomePage.HOME,
    val shipping: ShippingState = ShippingState()
)

enum class HomePage {
    HOME, SHIPPING, SHIPPING_HISTORY, AFTER_SALE, AFTER_SALE_HISTORY, MODIFY
}

data class ShippingState(
    val product: Product = Product(),
    val customer: Customer = Customer(),
    val software: Software = Software(),
    val equipment: Equipment = Equipment()
)