package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.model.Program
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.tx.tx
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/5/15 14:51
 */
class ProgramViewModel constructor(private val dao: ProgramDao) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgramUiState())
    private val _selected = MutableStateFlow(0L)
    private val _page = MutableStateFlow(PageType.LIST)
    private val _loading = MutableStateFlow(false)

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Combine the various flows into a single program UI state
            combine(
                dao.getAll(),
                _selected,
                _page,
                _loading,
            ) { entities, selected, page, loading ->
                ProgramUiState(
                    entities = entities,
                    selected = selected,
                    page = page,
                    loading = loading
                )
            }.catch { ex ->
                ex.printStackTrace()
            }.collect {
                // Set the program UI state
                _uiState.value = it
            }
        }
    }

    /**
     * Processes a program event and updates the program UI state accordingly.
     *
     * @param event The program event to process.
     */
    fun event(event: ProgramEvent) {
        when (event) {
            is ProgramEvent.NavTo -> _page.value = event.page
            is ProgramEvent.ToggleSelected -> _selected.value = event.id
            is ProgramEvent.Insert -> async { dao.insert(Program(text = event.name)) }
            is ProgramEvent.Update -> async { dao.update(event.entity) }
            is ProgramEvent.Delete -> async { dao.deleteById(event.id) }
            is ProgramEvent.MoveTo -> moveTo(event.id, event.distance)
        }
    }

    /**
     * Runs a suspend block of code asynchronously on the view model scope.
     *
     * @param block The suspend block of code to run.
     */
    private fun async(block: suspend () -> Unit) {
        viewModelScope.launch {
            // Execute the suspend block of code
            block()
        }
    }

    /**
     * Moves a program to a new position in the list.
     *
     * @param id The ID of the program to move.
     * @param distance The distance to move the program.
     */
    private fun moveTo(id: Int, distance: Float) {
        viewModelScope.launch {
            // Set the loading state to true
            _loading.value = true
            // Execute the transaction to move the program
            tx {
                move {
                    index = id
                    dv = distance
                }
            }
            // Set the loading state to false
            _loading.value = false
        }
    }
}

/**
 * Data class that represents the state of the program UI.
 *
 * @param entities The list of program entities to display.
 * @param selected The ID of the currently selected program.
 * @param page The current page being displayed.
 * @param loading Whether or not the UI is currently loading data.
 */
data class ProgramUiState(
    val entities: List<Program> = emptyList(),
    val selected: Long = 0L,
    val page: PageType = PageType.LIST,
    val loading: Boolean = false,
)

/**
 * Sealed class that represents events that can occur in the program UI.
 */
sealed class ProgramEvent {
    data class NavTo(val page: PageType) : ProgramEvent()
    data class ToggleSelected(val id: Long) : ProgramEvent()
    data class Insert(val name: String) : ProgramEvent()
    data class Update(val entity: Program) : ProgramEvent()
    data class Delete(val id: Long) : ProgramEvent()
    data class MoveTo(val id: Int, val distance: Float) : ProgramEvent()
}