package com.zktony.www.common.app

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.zktony.www.data.repository.CalibrationRepository
import com.zktony.www.data.repository.ContainerRepository
import com.zktony.www.data.repository.MotorRepository
import javax.inject.Inject

/**
 * 用于创建[AppViewModel]实例
 */
class AppViewModelFactory @Inject constructor(
    private val application: Application,
    private val dataStore: DataStore<Preferences>,
    private val motorRepo: MotorRepository,
    private val containerRepo: ContainerRepository,
    private val calibrationRepo: CalibrationRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when (modelClass) {
            AppViewModel::class.java -> AppViewModel(
                application,
                dataStore,
                motorRepo,
                containerRepo,
                calibrationRepo
            )
            else -> throw IllegalArgumentException("Unknown class $modelClass")
        } as T
    }
}