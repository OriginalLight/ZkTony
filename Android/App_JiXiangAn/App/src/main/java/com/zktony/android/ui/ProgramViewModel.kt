package com.zktony.android.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author 刘贺贺
 * @date 2023/5/15 14:51
 */
@HiltViewModel
class ProgramViewModel @Inject constructor(
    private val dao: ProgramDao
) : ViewModel() {

    private val _selected = MutableStateFlow(0L)
    private val _page = MutableStateFlow(PageType.PROGRAM)
    private val _uiFlags = MutableStateFlow<UiFlags>(UiFlags.none())

    val selected = _selected.asStateFlow()
    val page = _page.asStateFlow()
    val uiFlags = _uiFlags.asStateFlow()
    val entities = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 40)
    ) { dao.getByPage() }.flow.cachedIn(viewModelScope)

    private val _entityNum = MutableStateFlow(0)

    val entityNum = _entityNum.asStateFlow()


    fun dispatch(intent: ProgramIntent) {
        when (intent) {
            is ProgramIntent.NavTo -> _page.value = intent.page
            is ProgramIntent.Flags -> _uiFlags.value = intent.uiFlags
            is ProgramIntent.Selected -> _selected.value = intent.id
            is ProgramIntent.Insert -> viewModelScope.launch {
                dao.insert(
                    Program(
                        displayText = intent.name,
                        startRange = intent.startRange,
                        endRange = intent.endRange,
                        thickness = intent.thickness,
                        coagulant = intent.coagulant,
                        volume = intent.volume,
                        founder = intent.founder
                    )
                )
            }

            is ProgramIntent.Update -> viewModelScope.launch { dao.update(intent.entity) }
            is ProgramIntent.Delete -> viewModelScope.launch { dao.deleteById(intent.id) }
            is ProgramIntent.Export -> export(intent.path, intent.name, intent.text)

            is ProgramIntent.count -> viewModelScope.launch {
                _entityNum.value = dao.count()
            }
        }
    }


}

private fun export(path: String?, name: String, text: String) {


}


sealed class ProgramIntent {
    data class NavTo(val page: Int) : ProgramIntent()
    data class Flags(val uiFlags: UiFlags) : ProgramIntent()
    data class Selected(val id: Long) : ProgramIntent()
    data class Insert(
        val name: String,
        val startRange: Double,
        val endRange: Double,
        val thickness: String,
        val coagulant: Int,
        val volume: Double,
        val founder: String
    ) : ProgramIntent()

    data class Update(val entity: Program) : ProgramIntent()
    data class Delete(val id: Long) : ProgramIntent()
    data class Export(val path: String?, val name: String, val text: String) : ProgramIntent()
    data object count : ProgramIntent()
}