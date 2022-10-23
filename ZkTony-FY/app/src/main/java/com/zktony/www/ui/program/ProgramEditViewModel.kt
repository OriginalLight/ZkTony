package com.zktony.www.ui.program

import androidx.lifecycle.viewModelScope
import com.zktony.www.base.BaseViewModel
import com.zktony.www.data.entity.Action
import com.zktony.www.data.repository.ActionRepository
import com.zktony.www.data.repository.ProgramRepository
import com.zktony.www.model.enum.ActionEnum
import com.zktony.www.ui.program.model.ProgramEditIntent
import com.zktony.www.ui.program.model.ProgramEditState
import com.zktony.www.ui.program.model.ProgramEditUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ProgramEditViewModel @Inject constructor(
    private val programRepository: ProgramRepository,
    private val actionRepository: ActionRepository
) : BaseViewModel() {

    private val _state = MutableSharedFlow<ProgramEditState>()
    val state: SharedFlow<ProgramEditState> get() = _state
    private val intent = MutableSharedFlow<ProgramEditIntent>()

    private val _uiState = MutableStateFlow(ProgramEditUiState())
    val uiState: StateFlow<ProgramEditUiState> get() = _uiState

    init {
        viewModelScope.launch {
            intent.collect {
                when (it) {
                    is ProgramEditIntent.OnSwitchAction -> onSwitchAction(it.action)
                    is ProgramEditIntent.OnDeleteAction -> onDeleteAction(it.action)
                    is ProgramEditIntent.OnAddAction -> onAddAction()
                    is ProgramEditIntent.OnLoadActions -> onLoadActions(it.programId)
                    is ProgramEditIntent.OnEditAction -> onEditAction(it.action)
                }
            }
        }
    }

    /**
     * Intent处理器
     * @param intent [ProgramEditIntent]
     */
    fun dispatch(intent: ProgramEditIntent) {
        try {
            viewModelScope.launch {
                this@ProgramEditViewModel.intent.emit(intent)
            }
        } catch (_: Exception) {
        }
    }

    /**
     * 加载程序列表
     * @param programId [String] 程序ID
     */
    private fun onLoadActions(programId: String) {
        viewModelScope.launch {
            if (programId != "None") {
                _uiState.update { it.copy(programId = programId) }
            }
            actionRepository.getByProgramId(programId).collect {
                _state.emit(ProgramEditState.OnActionChange(it))
            }
        }
    }

    /**
     * 添加程序
     */
    private fun onAddAction() {
        viewModelScope.launch {
            val action = uiState.value.action
            action.id = UUID.randomUUID().toString()
            action.programId = uiState.value.programId
            actionRepository.insert(action)
            programRepository.updateActions(uiState.value.programId)
        }
    }

    /**
     * 删除步骤
     * @param action [Action] 步骤
     */
    private fun onDeleteAction(action: Action) {
        viewModelScope.launch {
            actionRepository.delete(action)
            programRepository.updateActions(uiState.value.programId)
        }
    }

    /**
     * 编辑步骤
     * @param action [Action] 步骤
     */
    private fun onEditAction(action: Action) {
        viewModelScope.launch {
            _uiState.update { it.copy(action = action) }
            _state.emit(ProgramEditState.OnButtonChange(validateAction(uiState.value.action)))
        }
    }

    /**
     * 切换步骤
     * @param action [ActionEnum] 步骤
     */
    private fun onSwitchAction(action: ActionEnum) {
        viewModelScope.launch {
            _uiState.update { it.copy(action = it.action.copy(mode = action.index)) }
            _state.emit(ProgramEditState.OnSwitchAction(action))
            _state.emit(ProgramEditState.OnButtonChange(validateAction(uiState.value.action)))
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