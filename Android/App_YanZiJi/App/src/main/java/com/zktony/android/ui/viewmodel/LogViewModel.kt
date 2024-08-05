package com.zktony.android.ui.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zktony.android.data.NameTimeRangeQuery
import com.zktony.android.data.defaults.defaultNameTimeRangeQuery
import com.zktony.android.ui.components.Tips
import com.zktony.android.utils.TipsUtils
import com.zktony.log.LogUtils
import com.zktony.room.repository.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
@HiltViewModel
class LogViewModel @Inject constructor(
    private val logRepository: LogRepository
) : ViewModel() {
    private val _selected = MutableStateFlow<List<Long>>(emptyList())
    private val _query = MutableStateFlow(defaultNameTimeRangeQuery())

    val selected = _selected.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("CheckResult")
    val entities = _query.flatMapLatest { query ->
        Pager(PagingConfig(pageSize = 20, initialLoadSize = 40)) {
            logRepository.getByPage(query.name, query.startTime, query.endTime)
        }.flow.cachedIn(viewModelScope)
    }

    // 删除
    suspend fun delete() {
        try {
            val ids = _selected.value
            if (ids.isEmpty()) {
                return
            }
            val res = logRepository.deleteByIds(ids)
            if (res) {
                _selected.value = emptyList()
                TipsUtils.showTips(Tips.info("删除成功"))
                LogUtils.info("删除成功 $ids", true)
            } else {
                TipsUtils.showTips(Tips.error("删除失败"))
                LogUtils.error("删除失败 $ids", true)
            }
        } catch (e: Exception) {
            LogUtils.error(e.stackTraceToString(), true)
            TipsUtils.showTips(Tips.error("删除失败"))
        }
    }

    // 单选
    fun select(id: Long) {
        val list = _selected.value.toMutableList()
        if (list.contains(id)) {
            list.remove(id)
        } else {
            list.add(id)
        }
        _selected.value = list
    }

    // 全选
    fun selectAll(ids: List<Long>) {
        viewModelScope.launch {
            _selected.value = ids
        }
    }

    // 搜索
    fun search(query: NameTimeRangeQuery) {
        _query.value = query
        _selected.value = emptyList()
        TipsUtils.showTips(Tips.info("搜索成功"))
    }
}

