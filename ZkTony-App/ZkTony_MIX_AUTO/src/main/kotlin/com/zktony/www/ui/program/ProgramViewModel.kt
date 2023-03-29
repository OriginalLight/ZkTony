package com.zktony.www.ui.program

import androidx.lifecycle.viewModelScope
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.base.BaseViewModel
import com.zktony.common.utils.Snowflake
import com.zktony.www.data.local.dao.HoleDao
import com.zktony.www.data.local.dao.PlateDao
import com.zktony.www.data.local.dao.ProgramDao
import com.zktony.www.data.local.entity.Program
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            dao.getAll().collect {
                _programList.value = it
            }
        }
    }

    fun delete(program: Program) {
        viewModelScope.launch {
            dao.delete(program)
            plateDao.getBySubId(program.id).collect {
                plateDao.deleteAll(it)
                it.forEach { p ->
                    holeDao.deleteBySubId(p.id)
                }
            }
        }
    }

    fun insert(name: String) {
        viewModelScope.launch {
            val work = workList.value.find { it.name == name }
            if (work != null) {
                PopTip.show("已存在相同名称的程序")
            } else {
                val snowflake = Snowflake(1)
                val program = Program(id = snowflake.nextId(), name = name)
                dao.insert(program)
                val plate = plateDao.getById(1L).firstOrNull()
                val holeList = holeDao.getBySubId(1L).firstOrNull()
                val newPlate = plate?.copy(
                    id = snowflake.nextId(),
                    subId = program.id,
                )
                val newHoleList = holeList?.map {
                    it.copy(
                        id = snowflake.nextId(),
                        subId = newPlate?.id ?: 0L
                    )
                }
                newPlate?.let {
                    plateDao.insert(it)
                    newHoleList?.let { list ->
                        holeDao.insertAll(list)
                    }
                }

            }
        }
    }
}