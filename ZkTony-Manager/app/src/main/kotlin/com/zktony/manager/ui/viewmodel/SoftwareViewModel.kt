package com.zktony.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.manager.common.ext.showShortToast
import com.zktony.manager.data.remote.grpc.CustomerGrpc
import com.zktony.manager.data.remote.grpc.SoftwareGrpc
import com.zktony.proto.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class SoftwareViewModel constructor(
    private val grpc: SoftwareGrpc
) : ViewModel() {

    private val _uiState = MutableStateFlow(SoftwareUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            grpc.getSoftwarePage(
                softwareRequestPage {
                    page = 1
                    pageSize = 20
                }
            ).catch { it.message.toString().showShortToast() }
                .collect {
                    _uiState.value = _uiState.value.copy(list = it.listList)
                }
        }
    }

    fun search(search: SoftwareSearch) {
        viewModelScope.launch {
            grpc.searchSoftware(search).catch {
                "查询失败".showShortToast()
            }.collect {
                _uiState.value = _uiState.value.copy(list = it.listList)
            }
        }
    }

    fun setSoftware(software: Software?) {
        _uiState.value = _uiState.value.copy(software = software)
    }

    fun insert(software: Software) {
        viewModelScope.launch {
            grpc.add(software).catch {
                "添加失败".showShortToast()
            }.collect {
                "添加成功".showShortToast()
                val list = _uiState.value.list.toMutableList()
                _uiState.value = _uiState.value.copy(list = list.plus(software))
            }
        }
    }

    fun update(software: Software) {
        viewModelScope.launch {
            grpc.update(software).catch {
                "更新失败".showShortToast()
            }.collect {
                "更新成功".showShortToast()
                val list = _uiState.value.list.toMutableList()
                list.removeAll { it.id == software.id }
                _uiState.value = _uiState.value.copy(list = list.plus(software))
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

data class SoftwareUiState(
    val list: List<Software> = emptyList(),
    val software: Software? = null
)