package com.zktony.www.ui.program

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.core.ext.Ext
import com.zktony.www.room.dao.PointDao
import com.zktony.www.room.dao.ProgramDao
import com.zktony.www.room.entity.Program
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class ProgramViewModel constructor(
    private val PD: PointDao,
    private val PGD: ProgramDao,
) : BaseViewModel() {

    private val _programList = MutableStateFlow<List<Program>>(emptyList())
    val workList = _programList.asStateFlow()

    init {
        viewModelScope.launch {
            PGD.getAll().distinctUntilChanged().collect {
                _programList.value = it
            }
        }
    }

    fun delete(program: Program) {
        viewModelScope.launch {
            PGD.delete(program)
            PD.deleteBySubId(program.id)
        }
    }

    fun insert(name: String, block: (Long) -> Unit) {
        viewModelScope.launch {
            if (name.isEmpty()) {
                PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.not_empty))
                return@launch
            }
            val work = workList.value.find { it.name == name }
            if (work != null) {
                PopTip.show(Ext.ctx.getString(com.zktony.core.R.string.already_exists))
            } else {
                val program = Program(name = name)
                PGD.insert(program)
                block(program.id)
            }
        }
    }
}