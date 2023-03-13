package com.zktony.www.common.app

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.zktony.www.data.local.room.dao.CalibrationDao
import com.zktony.www.data.local.room.dao.ContainerDao
import com.zktony.www.data.local.room.dao.MotorDao
import com.zktony.www.data.local.room.dao.PlateDao
import javax.inject.Inject

/**
 * 用于创建[AppViewModel]实例
 */
class AppViewModelFactory @Inject constructor(
    private val application: Application,
    private val dataStore: DataStore<Preferences>,
    private val motor: MotorDao,
    private val calibration: CalibrationDao,
    private val container: ContainerDao,
    private val plate: PlateDao,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when (modelClass) {
            AppViewModel::class.java -> AppViewModel(
                application = application,
                dataStore = dataStore,
                motor = motor,
                calibration = calibration,
                container = container,
                plate = plate,
            )
            else -> throw IllegalArgumentException("Unknown class $modelClass")
        } as T
    }
}