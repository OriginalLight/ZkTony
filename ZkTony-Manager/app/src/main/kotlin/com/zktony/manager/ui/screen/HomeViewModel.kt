package com.zktony.manager.ui.screen

import androidx.lifecycle.ViewModel
import com.zktony.manager.data.remote.model.Customer
import com.zktony.manager.data.remote.model.Equipment
import com.zktony.manager.data.remote.model.Product
import com.zktony.manager.data.remote.model.Software
import com.zktony.manager.data.store.SoftwareStore
import com.zktony.manager.ui.utils.DataManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
class HomeViewModel(
    private val softWareStore: SoftwareStore = DataManager.softwareStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

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