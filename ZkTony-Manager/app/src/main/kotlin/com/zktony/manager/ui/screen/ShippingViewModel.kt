package com.zktony.manager.ui.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.manager.data.repository.ApplicationRepository
import com.zktony.manager.data.repository.impl.ApplicationRepositoryImpl
import com.zktony.manager.data.remote.client.NetworkResult
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
class ShippingViewModel(
    private val applicationRepository: ApplicationRepository = ApplicationRepositoryImpl()
) : ViewModel() {
    init {
        viewModelScope.launch {
            applicationRepository.getApplicationById().collect {
                when (it) {
                    is NetworkResult.Success -> {
                        Log.d("ShippingViewModel", "getApplicationById: ${it.data}")
                    }
                    is NetworkResult.Error -> {
                        Log.d("ShippingViewModel", "getApplicationById: ${it.throwable}")
                    }
                    is NetworkResult.Loading -> {
                        Log.d("ShippingViewModel", "getApplicationById: loading")
                    }
                }
            }
        }
    }
}