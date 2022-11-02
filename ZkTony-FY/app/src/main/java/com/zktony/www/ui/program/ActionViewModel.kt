package com.zktony.www.ui.program

import androidx.lifecycle.viewModelScope
import com.zktony.www.base.BaseViewModel
import com.zktony.www.common.room.entity.Action
import com.zktony.www.common.room.entity.ActionEnum
import com.zktony.www.data.repository.ActionRepository
import com.zktony.www.data.repository.ProgramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ActionViewModel @Inject constructor(
    private val programRepository: ProgramRepository,
    private val actionRepository: ActionRepository
) : BaseViewModel() {

    private val _event = MutableSharedFlow<ActionEvent>()
    val event: SharedFlow<ActionEvent> get() = _event

    private val _uiState = MutableStateFlow(ActionUiState())
    val uiState: StateFlow<ActionUiState> get() = _uiState

    /**
     * 加载程序列表
     * @param programId [String] 程序ID
     */
    fun setProgramId(programId: String) {
        viewModelScope.launch {
            if (programId != "None") {
                _uiState.value = _uiState.value.copy(programId = programId)
            }
        }
    }

    /**
     * 添加程序
     */
    fun addAction() {
        viewModelScope.launch {
            actionRepository.insert(
                _uiState.value.action.copy(
                    id = UUID.randomUUID().toString(),
                    programId = _uiState.value.programId
                )
            )
            programRepository.updateActions(uiState.value.programId)
        }
    }

    /**
     * 删除步骤
     * @param action [Action] 步骤
     */
    fun deleteAction(action: Action) {
        viewModelScope.launch {
            actionRepository.delete(action)
            programRepository.updateActions(uiState.value.programId)
        }
    }

    /**
     * 编辑步骤
     * @param action [Action] 步骤
     */
    fun editAction(action: Action) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(action = action)
            _event.emit(ActionEvent.OnButtonChange(validateAction(uiState.value.action)))
        }
    }

    /**
     * 切换步骤
     * @param action [ActionEnum] 步骤
     */
    fun switchAction(action: ActionEnum) {
        viewModelScope.launch {
            _uiState.value =
                _uiState.value.copy(action = _uiState.value.action.copy(mode = action.index))
            _event.emit(ActionEvent.OnSwitchAction(action))
            _event.emit(ActionEvent.OnButtonChange(validateAction(uiState.value.action)))
        }
    }


    /**
     * 加载步骤列表
     */
    fun loadActionList() {
        viewModelScope.launch {
            actionRepository.getByProgramId(uiState.value.programId).collect {
                _event.emit(ActionEvent.OnActionChange(it))
            }
        }
    }

    /**
     * 验证步骤
     * @param action [Action] 步骤
     * @return [Boolean] 是否有效
     */
    private fun validateAction(action: Action): Boolean {
        return action.order > 0
                && action.time > 0f
                && action.temperature > 0
                && action.liquidVolume > 0
                && (action.mode != ActionEnum.WASHING.index || action.count > 0)
    }

}


sealed class ActionEvent {
    data class OnSwitchAction(val action: ActionEnum) : ActionEvent()
    data class OnActionChange(val actionList: List<Action>) : ActionEvent()
    data class OnButtonChange(val enable: Boolean) : ActionEvent()
}

data class ActionUiState(
    val programId: String = "",
    val action: Action = Action()
)