package com.zktony.manager.ui.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.manager.data.remote.client.NetworkResult
import com.zktony.manager.data.remote.model.Software
import com.zktony.manager.data.remote.model.SoftwareQueryDTO
import com.zktony.manager.data.store.SoftwareStore
import com.zktony.manager.ui.utils.DataManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
class ShippingViewModel(
    private val softWareStore: SoftwareStore = DataManager.softwareStore
) : ViewModel() {
    init {
        viewModelScope.launch {
            softWareStore.add(
                Software(
                    `package` = "com.zktony.manager",
                    version_name = "1.0.0",
                    version_code = 1,
                    build_type = "debug",
                )
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        println("success")
                    }
                    else -> {}
                }
            }

            delay(1000L)
            softWareStore.get(
                SoftwareQueryDTO(
                    build_type = "debug",
                )
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        Log.e("TAG", it.data.toString())
                    }
                    else -> {
                        println("else")
                    }
                }
            }
        }
    }
}