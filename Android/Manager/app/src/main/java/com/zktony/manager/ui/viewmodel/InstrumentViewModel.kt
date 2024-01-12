package com.zktony.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.manager.ext.showShortToast
import com.zktony.manager.data.remote.grpc.InstrumentGrpc
import com.zktony.proto.Instrument
import com.zktony.proto.InstrumentSearch
import com.zktony.proto.instrumentRequestPage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class InstrumentViewModel constructor(
    private val grpc: InstrumentGrpc
) : ViewModel() {

    private val _uiState = MutableStateFlow(InstrumentUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            grpc.getInstrumentPage(
                instrumentRequestPage {
                    page = 1
                    pageSize = 20
                }
            ).catch { it.message.toString().showShortToast() }
                .collect {
                    _uiState.value = _uiState.value.copy(list = it.listList)
                }
        }
    }

    fun search(search: InstrumentSearch) {
        viewModelScope.launch {
            grpc.searchInstrument(search).catch {
                "查询失败".showShortToast()
            }.collect {
                _uiState.value = _uiState.value.copy(list = it.listList)
            }
        }
    }

    fun setInstrument(instrument: Instrument?) {
        _uiState.value = _uiState.value.copy(instrument = instrument)
    }

    fun insert(instrument: Instrument) {
        viewModelScope.launch {
            grpc.add(instrument).catch {
                "添加失败".showShortToast()
            }.collect {
                "添加成功".showShortToast()
                val list = _uiState.value.list.toMutableList()
                _uiState.value = _uiState.value.copy(list = list.plus(instrument))
            }
        }
    }

    fun update(instrument: Instrument) {
        viewModelScope.launch {
            grpc.update(instrument).catch {
                "更新失败".showShortToast()
            }.collect {
                "更新成功".showShortToast()
                val list = _uiState.value.list.toMutableList()
                list.removeAll { it.id == instrument.id }
                _uiState.value = _uiState.value.copy(list = list.plus(instrument))
            }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            grpc.delete(id).catch {
                "删除失败".showShortToast()
            }.collect {
                "删除成功".showShortToast()
                val list = _uiState.value.list.toMutableList()
                list.removeAll { it.id == id }
                _uiState.value = _uiState.value.copy(list = list)
            }
        }
    }
}

data class InstrumentUiState(
    val list: List<Instrument> = emptyList(),
    val instrument: Instrument? = null
)