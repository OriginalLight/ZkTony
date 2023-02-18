package com.zktony.www.common.app

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.zktony.www.data.repository.CalibrationRepository
import com.zktony.www.data.repository.MotorRepository
import com.zktony.www.data.repository.PlateRepository
import javax.inject.Inject

/**
 * 用于创建[AppViewModel]实例
 */
class AppViewModelFactory @Inject constructor(
    private val application: Application,
    private val dataStore: DataStore<Preferences>,
    private val motorRepository: MotorRepository,
    private val calibrationRepository: CalibrationRepository,
    private val plateRepository: PlateRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when (modelClass) {
            AppViewModel::class.java -> AppViewModel(
                application,
                dataStore,
                motorRepository,
                calibrationRepository,
                plateRepository
            )
            else -> throw IllegalArgumentException("Unknown class $modelClass")
        } as T
    }
}