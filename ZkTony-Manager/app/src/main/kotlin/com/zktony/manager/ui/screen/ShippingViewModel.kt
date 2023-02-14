package com.zktony.manager.ui.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.manager.data.remote.client.NetworkResult
import com.zktony.manager.data.repository.ApplicationRepository
import com.zktony.manager.data.repository.impl.ApplicationRepositoryImpl
import kotlinx.coroutines.delay
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
            delay(30 * 1000)
            applicationRepository.getApplicationById(id = "com.zktony.www.zm.debug").collect {
                when (it) {
                    is NetworkResult.Success -> {
                        Log.d("ShippingViewModel", "getApplicationById: ${it.data}")
                        downloadApplication(it.data.download_url)
                    }
                    is NetworkResult.Error -> {
                        Log.d("ShippingViewModel", "getApplicationById: ${it.throwable}")
                    }
                    is NetworkResult.Loading -> {
                        Log.d("ShippingViewModel", "getApplicationById: loading")
                    }
                    else -> {}
                }
            }
        }
    }

    fun downloadApplication(url: String) {
        viewModelScope.launch {
            applicationRepository.downloadApplication(url = url).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        Log.d("ShippingViewModel", "downloadApplication: ${it.data}")
                    }
                    is NetworkResult.Error -> {
                        Log.d("ShippingViewModel", "downloadApplication: ${it.throwable}")
                    }
                    is NetworkResult.Loading -> {
                        Log.d("ShippingViewModel", "downloadApplication: loading")
                    }
                    is NetworkResult.Progress -> {
                        Log.d("ShippingViewModel", "downloadApplication: ${it.progress}")
                    }
                }
            }
        }
    }

}