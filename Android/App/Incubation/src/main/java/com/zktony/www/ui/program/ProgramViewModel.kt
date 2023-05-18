package com.zktony.www.ui.program

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.Ext
import com.zktony.www.data.dao.ActionDao
import com.zktony.www.data.dao.ProgramDao
import com.zktony.www.data.entities.Program
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProgramViewModel constructor(
    private val PD: ProgramDao,
    private val AD: ActionDao
) : BaseViewModel() {
    private val _programList = MutableStateFlow(emptyList<Program>())
    val programList = _programList.asStateFlow()

    init {
        viewModelScope.launch {
            PD.getAll().collect {
                _programList.value = it
            }
        }
    }

    /**
     * 添加程序
     * @param name [String] 程序名
     */
    fun insert(name: String, block: (String) -> Unit) {
        viewModelScope.launch {
            if (name.isEmpty()) {
                PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.not_empty))
                return@launch
            }
            PD.getByName(name).firstOrNull()?.let {
                PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.already_exists))
                return@launch
            }
            val program = Program(name = name)
            PD.insert(program)
            block(program.id)
        }
    }

    /**
     * 删除程序
     * @param entity [Program] 程序
     */
    fun delete(entity: Program) {
        viewModelScope.launch {
            PD.delete(entity)
            AD.deleteByProgramId(entity.id)
        }
    }
}