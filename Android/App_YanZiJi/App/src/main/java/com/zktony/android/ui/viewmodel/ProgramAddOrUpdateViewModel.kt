package com.zktony.android.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.room.entities.Program
import com.zktony.room.repository.ProgramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgramAddOrUpdateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    programRepository: ProgramRepository
) : ViewModel() {
    private val id: Long = checkNotNull(savedStateHandle["id"])
    private val _navObj = MutableStateFlow<Program?>(null)
    val navObj = _navObj.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (id > 0) {
                _navObj.value = programRepository.getById(id)
            }
        }
    }
}