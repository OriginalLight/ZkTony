package com.zktony.www.ui.program

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseViewModel
import com.zktony.www.room.dao.HoleDao
import com.zktony.www.room.dao.PlateDao
import com.zktony.www.room.dao.ProgramDao
import com.zktony.www.room.entity.Program
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ProgramViewModel constructor(
    private val dao: ProgramDao,
    private val plateDao: PlateDao,
    private val holeDao: HoleDao
) : BaseViewModel() {

    private val _programList = MutableStateFlow<List<Program>>(emptyList())
    val workList = _programList.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getAll().distinctUntilChanged().collect {
                _programList.value = it
            }
        }
    }

    fun delete(program: Program) {
        viewModelScope.launch {
            dao.delete(program)
            val plates = plateDao.getBySubId(program.id).firstOrNull() ?: emptyList()
            if (plates.isNotEmpty()) {
                plateDao.deleteAll(plates)
                plates.forEach {
                    holeDao.deleteBySubId(it.id)
                }
            }
        }
    }

    fun insert(name: String, block: (Long) -> Unit) {
        viewModelScope.launch {
            val work = workList.value.find { it.name == name }
            if (work != null) {
                PopTip.show("已存在相同名称的程序")
            } else {
                val program = Program(name = name)
                dao.insert(program)
                block(program.id)
            }
        }
    }
}