package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zktony.android.data.dao.ExperimentRecordDao
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.entities.ExperimentRecord
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
class ExperimentRecordsViewModel @Inject constructor(
    private val dao: ExperimentRecordDao
) : ViewModel() {

    private val _selected = MutableStateFlow(0L)
    private val _page = MutableStateFlow(PageType.EXPERIMENTRECORDS)
    private val _uiFlags = MutableStateFlow<UiFlags>(UiFlags.none())

    val selected = _selected.asStateFlow()
    val page = _page.asStateFlow()
    val uiFlags = _uiFlags.asStateFlow()
    val entities = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 40)
    ) { dao.getByPage() }.flow.cachedIn(viewModelScope)

    fun dispatch(intent: ExperimentRecordsIntent) {
        when (intent) {
            is ExperimentRecordsIntent.NavTo -> _page.value = intent.page
            is ExperimentRecordsIntent.Flags -> _uiFlags.value = intent.uiFlags
            is ExperimentRecordsIntent.Selected -> _selected.value = intent.id
            is ExperimentRecordsIntent.Insert -> viewModelScope.launch {
                dao.insert(
                    ExperimentRecord(
                        startRange = intent.startRange,
                        endRange = intent.endRange,
                        thickness = intent.thickness,
                        coagulant = intent.coagulant,
                        volume = intent.volume,
                        number = intent.number,
                        status=intent.status,
                        detail=intent.detail,
                    )
                )
            }

            is ExperimentRecordsIntent.Update -> viewModelScope.launch { dao.update(intent.entity) }
            is ExperimentRecordsIntent.Delete -> viewModelScope.launch { dao.deleteById(intent.id) }
        }
    }
}


sealed class ExperimentRecordsIntent {
    data class NavTo(val page: Int) : ExperimentRecordsIntent()
    data class Flags(val uiFlags: UiFlags) : ExperimentRecordsIntent()
    data class Selected(val id: Long) : ExperimentRecordsIntent()
    data class Insert(
        val startRange: Double,
        val endRange: Double,
        val thickness: String,
        val coagulant: Double,
        val volume: Double,
        val number: Int,
        val status: String,
        val detail: String
    ) : ExperimentRecordsIntent()

    data class Update(val entity: ExperimentRecord) : ExperimentRecordsIntent()
    data class Delete(val id: Long) : ExperimentRecordsIntent()
}