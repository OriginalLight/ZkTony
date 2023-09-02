package com.zktony.android.utils.service

import com.zktony.android.data.dao.HistoryDao
import com.zktony.android.data.entities.History
import kotlinx.coroutines.launch

class HistoryService(private val dao: HistoryDao) : AbstractService() {

    /**
     * 初始化历史记录
     * 1. 从数据库中获取所有历史记录
     * 2. 清理180天前的数据
     */
    override fun start() {
        scope.launch { dao.getAll().collect { cleanup(it) } }
    }

    /**
     * 清理180天前的数据
     *
     * @param historyList List<History>
     */
    private fun cleanup(historyList: List<History>) {
        // 清理180天前的数据
        val now = System.currentTimeMillis()
        val expired = now - 180 * 24 * 60 * 60 * 1000L
        val expiredList = historyList.filter { it.createTime.time < expired }
        if (expiredList.isNotEmpty()) {
            scope.launch {
                dao.deleteAll(expiredList)
            }
        }
    }
}