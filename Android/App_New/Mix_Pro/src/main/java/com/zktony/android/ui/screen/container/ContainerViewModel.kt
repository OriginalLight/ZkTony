package com.zktony.android.ui.screen.container

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.ContainerDao
import com.zktony.android.data.entity.Container
import com.zktony.android.data.entity.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/5/11 14:48
 */
class ContainerViewModel constructor(
    private val dao: ContainerDao,
) : ViewModel() {

    fun entities() = dao.getAll()

    fun insert(name: String) {
        viewModelScope.launch {
            val list: MutableList<Point> = mutableListOf()
            for (i in 0 until 6) {
                list.add(Point(index = i))
            }
            dao.insert(Container(name = name, data = list))
        }
    }

    fun delete(entity: Container) {
        viewModelScope.launch {
            dao.delete(entity)
        }
    }

    fun update(container: Container) {
        viewModelScope.launch {
            dao.update(container)
        }
    }
}