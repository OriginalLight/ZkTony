package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.MotorDao
import com.zktony.android.data.model.Motor
import com.zktony.android.ui.utils.PageType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 */
class MotorViewModel constructor(
    private val dao: MotorDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(MotorUiState())
    private val _selected = MutableStateFlow(0L)
    private val _page = MutableStateFlow(PageType.LIST)
    val uiState = _uiState.asStateFlow()

    /**
     * Initializes the MotorViewModel by setting up the initial state of the UI.
     */
    init {
        viewModelScope.launch {
            combine(
                dao.getAll(),
                _selected,
                _page,
            ) { entities, selected, page ->
                MotorUiState(entities = entities, selected = selected, page = page)
            }.catch { ex ->
                ex.printStackTrace()
            }.collect {
                _uiState.value = it
            }
        }
    }

    /**
     * The event function for the MotorViewModel class.
     * This function is used to handle events that occur on the screen.
     *
     * @param event The event to handle.
     */
    fun event(event: MotorEvent) {
        when (event) {
            is MotorEvent.NavTo -> _page.value = event.page // Step 1: Navigate to a different page
            is MotorEvent.ToggleSelected -> _selected.value =
                event.id // Step 2: Toggle the selected state of an entity
            is MotorEvent.Update -> async { dao.update(event.entity) } // Step 3: Update an entity
        }
    }

    /**
     * The async function for the MotorViewModel class.
     * This function is used to launch a coroutine in the viewModelScope.
     *
     * @param block The suspend block to execute.
     */
    private fun async(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
        }
    }
}

/**
 * The MotorUiState data class for the Motor screen.
 * This class defines the UI state of the screen.
 *
 * @param entities The list of entities to display on the screen.
 * @param selected The ID of the selected entity.
 * @param page The current page of the screen.
 */
data class MotorUiState(
    val entities: List<Motor> = emptyList(),
    val selected: Long = 0L,
    val page: PageType = PageType.LIST,
)

/**
 * The MotorEvent sealed class for the Motor screen.
 * This class defines the possible events that can occur on the screen.
 */
sealed class MotorEvent {
    data class NavTo(val page: PageType) : MotorEvent()
    data class ToggleSelected(val id: Long) : MotorEvent()
    data class Update(val entity: Motor) : MotorEvent()
}