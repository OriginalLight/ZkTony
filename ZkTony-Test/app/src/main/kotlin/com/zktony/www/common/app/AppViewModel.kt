package com.zktony.www.common.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * [Application] 生命周期内的 [AndroidViewModel]
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    application: Application,
) : AndroidViewModel(application)

