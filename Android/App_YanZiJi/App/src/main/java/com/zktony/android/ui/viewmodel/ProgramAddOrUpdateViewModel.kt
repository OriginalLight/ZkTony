package com.zktony.android.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.ui.components.Tips
import com.zktony.android.utils.TipsUtils
import com.zktony.log.LogUtils
import com.zktony.room.entities.Program
import com.zktony.room.repository.ProgramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgramAddOrUpdateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val programRepository: ProgramRepository
) : ViewModel() {

    private val id: Long = checkNotNull(savedStateHandle["id"])
    private val _navObj = MutableStateFlow<Program?>(null)
    val navObj = _navObj.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (id > 0) {
                _navObj.value = programRepository.getById(id)
            }
        }
    }

    suspend fun add(obj: Program): Boolean {
        try {
            if (programRepository.insert(obj)) {
                TipsUtils.showTips(Tips.info("添加成功"))
                return true
            } else {
                TipsUtils.showTips(Tips.error("添加失败"))
            }

        } catch (e: Exception) {
            when (e.message) {
                "1" -> TipsUtils.showTips(Tips.error("名称已存在"))
                else -> TipsUtils.showTips(Tips.error("添加失败"))
            }
            LogUtils.error(e.stackTraceToString(), true)
        }

        return false
    }

    suspend fun update(obj: Program): Boolean {
        try {
            if (programRepository.update(obj)) {
                TipsUtils.showTips(Tips.info("更新成功"))
                return true
            } else {
                TipsUtils.showTips(Tips.error("更新失败"))
            }
        } catch (e: Exception) {
            when (e.message) {
                "1" -> TipsUtils.showTips(Tips.error("名称已存在"))
                else -> TipsUtils.showTips(Tips.error("更新失败"))
            }
            LogUtils.error(e.stackTraceToString(), true)
        }

        return false
    }
}