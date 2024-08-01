package com.zktony.android.ui.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zktony.android.data.ProgramQuery
import com.zktony.android.data.defaults.defaultProgramQuery
import com.zktony.android.ui.components.Tips
import com.zktony.android.utils.JsonUtils
import com.zktony.android.utils.StorageUtils
import com.zktony.android.utils.TipsUtils
import com.zktony.android.utils.extra.dateFormat
import com.zktony.log.LogUtils
import com.zktony.room.entities.Program
import com.zktony.room.repository.ProgramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * @author 刘贺贺
 * @date 2023/5/15 14:51
 */
@HiltViewModel
class ProgramViewModel @Inject constructor(
    private val programRepository: ProgramRepository
) : ViewModel() {

    private val _selected = MutableStateFlow<List<Long>>(emptyList())
    private val _query = MutableStateFlow(defaultProgramQuery())

    val selected = _selected.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("CheckResult")
    val entities = _query.flatMapLatest { query ->
        Pager(PagingConfig(pageSize = 20, initialLoadSize = 40)) {
            programRepository.getByPage(query.name, query.startTime, query.endTime)
        }.flow.cachedIn(viewModelScope)
    }

    // 删除
    suspend fun delete() {
        try {
            val ids = _selected.value
            if (ids.isEmpty()) {
                return
            }
            val res = programRepository.deleteByIds(ids)
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

    // 获取参数文件
    fun getProgramFiles(): List<File>? {
        val usbList = StorageUtils.getUsbStorageDir()
        if (usbList.isEmpty()) {
            TipsUtils.showTips(Tips.error("未检测到U盘"))
            return null
        }

        try {
            val fileList = mutableListOf<File>()
            val dir = usbList.first() + "/${StorageUtils.ROOT_DIR}/${StorageUtils.PROGRAM_DIR}"
            val file = File(dir)

            if (file.exists() && file.isDirectory) {
                file.listFiles()?.forEach {
                    if (it.isFile && it.name.endsWith(".json")) {
                        fileList.add(it)
                    }
                }
            }

            if (fileList.isEmpty()) {
                TipsUtils.showTips(Tips.error("未检测到程序文件"))
                return null
            }
            return fileList
        } catch (e: Exception) {
            LogUtils.error(e.stackTraceToString(), true)
            TipsUtils.showTips(Tips.error("未知错误"))
            return null
        }
    }

    // 导入参数
    suspend fun import(file: File?) {
        try {
            if (file == null) {
                TipsUtils.showTips(Tips.error("参数文件不存在"))
                return
            }
            val json = withContext(Dispatchers.IO) {
                file.readText()
            }
            val programs = JsonUtils.fromJson<List<Program>>(json)
            if (programs.isEmpty()) {
                TipsUtils.showTips(Tips.error("文件格式错误"))
                return
            }

            programs.forEach {
                programRepository.insert(it.copy(id = 0L, createTime = System.currentTimeMillis()))
            }
            TipsUtils.showTips(Tips.info("导入程序成功"))

        } catch (e: Exception) {
            when (e.message) {
                "1" -> {
                    TipsUtils.showTips(Tips.error("程序名重复"))
                }

                else -> {
                    TipsUtils.showTips(Tips.error("未知错误"))
                }
            }
            LogUtils.error(e.stackTraceToString(), true)
        }
    }

    // 导出参数
    suspend fun export(pl: List<Program>) {
        try {
            if (pl.isEmpty()) {
                return
            }

            val usbList = StorageUtils.getUsbStorageDir()
            if (usbList.isEmpty()) {
                TipsUtils.showTips(Tips.error("未检测到U盘"))
                return
            }

            val dstDir = usbList.first() + "/${StorageUtils.ROOT_DIR}/${StorageUtils.PROGRAM_DIR}"
            if (!File(dstDir).exists()) {
                File(dstDir).mkdirs()
            }

            val json = JsonUtils.toJson(pl)
            val fileName = System.currentTimeMillis().dateFormat("yyyyMMddHHmmss") + ".json"

            withContext(Dispatchers.IO) {
                val file = File("$dstDir/$fileName")
                if (!file.exists()) {
                    file.canonicalFile
                }
                file.writeText(json)
            }

            TipsUtils.showTips(Tips.info("导出成功"))
        } catch (e: Exception) {
            LogUtils.error(e.stackTraceToString(), true)
            TipsUtils.showTips(Tips.error("导出失败"))
        }
    }

    // 搜索
    fun search(query: ProgramQuery) {
        _query.value = query
        _selected.value = emptyList()
        TipsUtils.showTips(Tips.info("搜索成功"))
    }

}