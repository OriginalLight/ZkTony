package com.zktony.android.utils.service

import com.zktony.android.data.dao.HistoryDao
import com.zktony.android.data.entities.History
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
        scope.launch { dao.getAll().collect { cleanup(it) } }
    }

    /**
     * 清理180天前的数据
     *
     * @param historyList List<History>
     */
    private fun cleanup(historyList: List<History>) {
        val now = System.currentTimeMillis()
        val expired = now - DAYS_180_MS
        val expiredList = historyList.filter { it.createTime.time < expired }
        expiredList.takeIf { it.isNotEmpty() }?.let { listToDelete ->
            scope.launch {
                dao.deleteAll(listToDelete)
            }
        }
    }


    companion object {
        private const val DAYS_180_MS = 180 * 24 * 60 * 60 * 1000L
    }
}