package com.zktony.www.common.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.www.ui.home.model.Cmd
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * [Application]生命周期内的[AndroidViewModel]
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _state = MutableSharedFlow<AppState>()
    val state: SharedFlow<AppState> get() = _state
    private val intent = MutableSharedFlow<AppIntent>()
    var latestSendCmd = Cmd()
    var latestReceiveCmd = Cmd()

    init {
        viewModelScope.launch {
            intent.collect {
                when (it) {
                    is AppIntent.SendCmd -> sendCmd(it.cmd)
                    is AppIntent.ReceiveCmd -> receiveCmd(it.cmd)
                }
            }
        }

    }

    fun dispatch(intent: AppIntent) {
        try {
            viewModelScope.launch {
                this@AppViewModel.intent.emit(intent)
            }
        } catch (_: Exception) {
        }
    }

    private fun sendCmd(cmd: Cmd) {
        latestSendCmd = cmd
        viewModelScope.launch {
            _state.emit(AppState.SendCmd(cmd))
        }
    }

    private fun receiveCmd(cmd: Cmd) {
        latestReceiveCmd = cmd
        viewModelScope.launch {
            _state.emit(AppState.ReceiveCmd(cmd))
        }
    }
}