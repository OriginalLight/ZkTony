package com.zktony.www.ui.program

import androidx.lifecycle.viewModelScope
import com.zktony.common.base.BaseViewModel
import com.zktony.www.data.local.dao.ActionDao
import com.zktony.www.data.local.dao.ProgramDao
import com.zktony.www.data.local.entity.Action
import com.zktony.www.data.local.entity.ActionEnum
import com.zktony.www.data.local.entity.getActionEnum
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.*

class ActionViewModel constructor(
    private val programDao: ProgramDao,
    private val actionDao: ActionDao
) : BaseViewModel() {

    private val _actionList = MutableStateFlow(emptyList<Action>())
    private val _action = MutableStateFlow(Action())
    private val _buttonEnable = MutableStateFlow(false)
    private val _programId = MutableStateFlow("None")
    val actionList = _actionList.asStateFlow()
    val action = _action.asStateFlow()
    val buttonEnable = _buttonEnable.asStateFlow()
    private val programId = _programId.asStateFlow()

    init {
        viewModelScope.launch {
            programId.collect {
                if (it != "None") {
                    actionDao.getByProgramId(it).collect { actionList ->
                        _actionList.value = actionList
                    }
                }
            }
        }
    }

    /**
     * 加载程序列表
     * @param id [String] 程序ID
     */
    fun load(id: String) {
        viewModelScope.launch {
            _programId.value = id
        }
    }

    /**
     * 添加程序
     */
    fun insert() {
        viewModelScope.launch {
            actionDao.insert(
                _action.value.copy(
                    id = UUID.randomUUID().toString(),
                    programId = programId.value
                )
            )
            delay(500L)
            updateActions()
        }
    }

    /**
     * 删除步骤
     * @param action [Action] 步骤
     */
    fun delete(action: Action) {
        viewModelScope.launch {
            actionDao.delete(action)
            delay(500L)
            updateActions()
        }
    }

    /**
     * 更新程序中的actions
     */
    private fun updateActions() {
        viewModelScope.launch {
            actionDao.getByProgramId(programId.value).firstOrNull()?.let {
                val str = StringBuilder()
                if (it.isNotEmpty()) {
                    it.forEachIndexed { index, action ->
                        str.append(getActionEnum(action.mode).value)
                        if (index != it.size - 1) {
                            str.append(" -> ")
                        }
                    }
                } else {
                    str.append("没有任何操作，去添加吧...")
                }
                programDao.getById(programId.value).firstOrNull()?.let { program ->
                    programDao.update(
                        program.copy(
                            actions = str.toString(),
                            actionCount = it.size
                        )
                    )
                }
            }
        }
    }

    /**
     * 编辑步骤
     * @param action [Action] 步骤
     */
    fun editAction(action: Action) {
        viewModelScope.launch {
            _action.value = action
            validateAction(action)
        }
    }

    /**
     * 切换步骤
     * @param action [ActionEnum] 步骤
     */
    fun switchAction(action: ActionEnum) {
        viewModelScope.launch {
            _action.value = _action.value.copy(mode = action.index)
            validateAction(this@ActionViewModel.action.value)
        }
    }

    /**
     * 验证步骤
     * @param action [Action] 步骤
     * @return [Boolean] 是否有效
     */
    private fun validateAction(action: Action) {
        _buttonEnable.value = action.order > 0
                && action.time > 0f
                && action.temperature > 0
                && action.liquidVolume > 0
                && (action.mode != ActionEnum.WASHING.index || action.count > 0)
    }

}