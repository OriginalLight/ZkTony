package com.zktony.www.ui.home

import com.zktony.common.base.BaseViewModel
import com.zktony.www.common.app.AppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : BaseViewModel() {

    @Inject
    lateinit var appViewModel: AppViewModel

}