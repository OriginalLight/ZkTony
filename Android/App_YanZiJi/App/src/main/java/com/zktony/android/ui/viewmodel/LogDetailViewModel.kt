package com.zktony.android.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.ui.components.Tips
import com.zktony.android.utils.PdfUtils
import com.zktony.android.utils.StorageUtils
import com.zktony.android.utils.TipsUtils
import com.zktony.android.utils.extra.dateFormat
import com.zktony.log.LogUtils
import com.zktony.room.entities.Log
import com.zktony.room.entities.LogSnapshot
import com.zktony.room.repository.LogRepository
import com.zktony.room.repository.LogSnapshotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class LogDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val logRepository: LogRepository,
    private val logSnapshotRepository: LogSnapshotRepository
) : ViewModel() {

    private val id: Long = checkNotNull(savedStateHandle["id"])
    private val _navObj = MutableStateFlow<Log?>(null)
    private val _entities = MutableStateFlow<List<LogSnapshot>>(emptyList())

    val navObj = _navObj.asStateFlow()
    val entities = _entities.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _navObj.value = logRepository.getById(id)
            logSnapshotRepository.getBySubId(id).collect {
                _entities.value = it
            }
        }
    }

    suspend fun export() {
        try {
            val log = _navObj.value ?: return
            val snapshots = _entities.value

            val usbList = StorageUtils.getUsbStorageDir()
            if (usbList.isEmpty()) {
                TipsUtils.showTips(Tips.error("未检测到U盘"))
                return
            }

            val dstFile = usbList.first() + "/${log.createTime.dateFormat("yyyyMMddHHmmss")}.pdf"
            val file = File(dstFile)
            if (!file.exists()) {
                withContext(Dispatchers.IO) {
                    file.createNewFile()
                }
            }

            withContext(Dispatchers.IO) {
                PdfUtils.generatePdf(file, log, snapshots)
            }
            TipsUtils.showTips(Tips.info("导出成功"))
        } catch (e: Exception) {
            TipsUtils.showTips(Tips.error("导出失败"))
            LogUtils.error(e.stackTraceToString(), true)
        }
    }
}