package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _selected = MutableStateFlow<List<Program>>(emptyList())

    val selected = _selected.asStateFlow()
    val entities = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 40),
    ) { programRepository.getByPage() }.flow.cachedIn(
        viewModelScope
    )

    fun select(program: Program) {
        val list = _selected.value.toMutableList()
        if (list.contains(program)) {
            list.remove(program)
        } else {
            list.add(program)
        }
        _selected.value = list
    }

    fun selectAll(list: List<Program>) {
        viewModelScope.launch {
            _selected.value = list
            LogUtils.info("selectAll: ${list.size}")
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

            withContext(Dispatchers.IO) {
                programs.forEach {
                    val res = programRepository.insert(it.copy(id = 0L))
                    if (res.isFailure) {
                        val message = when (res.exceptionOrNull()?.message) {
                            "1" -> "程序名重复"
                            "2" -> "插入数据库失败"
                            else -> "未知错误"
                        }
                        TipsUtils.showTips(Tips.error("导入程序失败 $message"))
                        return@withContext
                    }
                }
                TipsUtils.showTips(Tips.info("导入程序成功"))
            }

        } catch (e: Exception) {
            LogUtils.error(e.stackTraceToString(), true)
            TipsUtils.showTips(Tips.error("导入程序失败"))
        }
    }

    suspend fun export() {
        try {
            val selected = _selected.value
            if (selected.isEmpty()) {
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

            val json = JsonUtils.toJson(selected)
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
}