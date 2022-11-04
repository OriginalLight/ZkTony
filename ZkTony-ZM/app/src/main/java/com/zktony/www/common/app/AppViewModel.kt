package com.zktony.www.common.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.www.ui.home.model.Cmd
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * [Application]生命周期内的[AndroidViewModel]
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _event = MutableSharedFlow<AppEvent>()
    val event = _event.asSharedFlow()

    var latestSendCmd = Cmd()
    var latestReceiveCmd = Cmd()


    fun sendCmd(cmd: Cmd) {
        latestSendCmd = cmd
        viewModelScope.launch {
            _event.emit(AppEvent.SendCmd(cmd))
        }
    }

    fun receiveCmd(cmd: Cmd) {
        latestReceiveCmd = cmd
        viewModelScope.launch {
            _event.emit(AppEvent.ReceiveCmd(cmd))
        }
    }
}

sealed class AppEvent {
    data class SendCmd(val cmd: Cmd) : AppEvent()
    data class ReceiveCmd(val cmd: Cmd) : AppEvent()
}