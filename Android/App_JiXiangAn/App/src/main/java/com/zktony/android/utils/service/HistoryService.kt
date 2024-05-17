package com.zktony.android.utils.service

import com.zktony.android.data.dao.HistoryDao
import com.zktony.android.data.entities.History
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class HistoryService @Inject constructor(
    private val dao: HistoryDao
) : AbstractService() {

    /**
     * 初始化历史记录
     * 1. 从数据库中获取所有历史记录
     * 2. 清理180天前的数据
     */
    override fun create() {
        scope.launch {
            val historyList = dao.getAll().first()
            val now = System.currentTimeMillis()
            val expired = now - 180 * 24 * 60 * 60 * 1000L
            val expiredList = historyList.filter { it.createTime.time < expired }
            expiredList.takeIf { it.isNotEmpty() }?.let { listToDelete ->
                dao.deleteAll(listToDelete)
            }
        }
    }
}