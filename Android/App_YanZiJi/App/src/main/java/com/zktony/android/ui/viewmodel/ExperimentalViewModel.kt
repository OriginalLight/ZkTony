package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zktony.android.data.ExperimentalControl
import com.zktony.android.data.ExperimentalState
import com.zktony.android.ui.components.Tips
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.SerialPortUtils
import com.zktony.android.utils.TipsUtils
import com.zktony.room.entities.Program
import com.zktony.room.repository.ProgramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExperimentalViewModel @Inject constructor(
    private val programRepository: ProgramRepository
) : ViewModel() {

    val entities = Pager(PagingConfig(pageSize = 20, initialLoadSize = 40)) {
        programRepository.getByPage()
    }.flow.cachedIn(viewModelScope)


    fun updateProgram(channel: Int, program: Program) {
        AppStateUtils.setChannelProgramList(AppStateUtils.channelProgramList.value.mapIndexed { index, state ->
            if (index == channel) {
                program
            } else {
                state
            }
        })
    }

    suspend fun startExperiment(channel: Int, experimental: ExperimentalControl): Boolean {
        if (!SerialPortUtils.setExperimentalArguments(channel, experimental)) {
            TipsUtils.showTips(Tips.error("实验参数设置失败 通道：${channel + 1}"))
            return false
        } else {
            TipsUtils.showTips(Tips.info("实验参数设置成功 通道：${channel + 1}"))
        }

        if (!SerialPortUtils.setExperimentalState(channel, 1)) {
            TipsUtils.showTips(Tips.error("实验开始失败 通道：${channel + 1}"))
            return false
        } else {
            TipsUtils.showTips(Tips.info("实验开始成功 通道：${channel + 1}"))
            AppStateUtils.transformState(channel, ExperimentalState.STARTING)
            return true
        }
    }

    suspend fun pauseExperiment(channel: Int): Boolean {
        if (!SerialPortUtils.setExperimentalState(channel, 2)) {
            TipsUtils.showTips(Tips.error("实验暂停失败 通道：${channel + 1}"))
            return false
        } else {
            TipsUtils.showTips(Tips.info("实验暂停成功 通道：${channel + 1}"))
            AppStateUtils.transformState(channel, ExperimentalState.PAUSE)
            return true
        }
    }

    suspend fun stopExperiment(channel: Int, experimentalType: Int): Boolean {
        if (!SerialPortUtils.setExperimentalState(channel, 3)) {
            TipsUtils.showTips(Tips.error("实验停止失败 通道：${channel + 1}"))
            return false
        } else {
            TipsUtils.showTips(Tips.info("实验停止成功 通道：${channel + 1}"))
            AppStateUtils.transformState(
                channel,
                if (experimentalType == 0) ExperimentalState.DRAIN else ExperimentalState.READY
            )
            return true
        }
    }

}