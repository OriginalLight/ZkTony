package com.zktony.www.manager

import com.zktony.core.utils.Snowflake
import com.zktony.core.ext.logi
import com.zktony.www.room.dao.ContainerDao
import com.zktony.www.room.dao.HoleDao
import com.zktony.www.room.dao.PlateDao
import com.zktony.www.room.entity.Container
import com.zktony.www.room.entity.Hole
import com.zktony.www.room.entity.Plate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ContainerManager constructor(
    private val containerDao: ContainerDao,
    private val plateDao: PlateDao,
    private val holeDao: HoleDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    init {
        scope.launch {
            containerDao.getAll().collect {
                if (it.isEmpty()) {
                    containerDao.insert(Container(id = 1L, name = "默认容器"))
                    val plate1 = Plate(id = 1L, subId = 1L, index = 0)
                    val plate2 = Plate(id = 2L, subId = 1L, index = 1)
                    val plate3 = Plate(id = 3L, subId = 1L, index = 2)
                    val plate4 = Plate(id = 4L, subId = 1L, index = 3)
                    plateDao.insertAll(listOf(plate1, plate2, plate3, plate4))
                    delay(10L)
                    initHole(plate1)
                    delay(10L)
                    initHole(plate2)
                    delay(10L)
                    initHole(plate3)
                    delay(10L)
                    initHole(plate4)
                }
            }
        }
    }

    private suspend fun initHole(plate: Plate) {
        val snowflake = Snowflake(2)
        val holes = mutableListOf<Hole>()
        for (i in 0 until plate.x) {
            for (j in 0 until plate.y) {
                holes.add(Hole(id = snowflake.nextId(), subId = plate.id, x = i, y = j))
            }
        }
        holeDao.insertAll(holes)
    }

    fun test() {
        scope.launch { "ContainerManager test".logi() }
    }
}