package com.zktony.www.common.app

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import javax.inject.Inject

/**
 * 用于创建[AppViewModel]实例
 */
class AppViewModelFactory @Inject constructor(
    private val application: Application,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when (modelClass) {
            AppViewModel::class.java -> AppViewModel(application)
            else -> throw IllegalArgumentException("Unknown class $modelClass")
        } as T
    }
}