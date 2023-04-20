package com.zktony.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.manager.ext.showShortToast
import com.zktony.manager.data.remote.grpc.CustomerGrpc
import com.zktony.proto.Customer
import com.zktony.proto.CustomerSearch
import com.zktony.proto.customerRequestPage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class CustomerViewModel constructor(
    private val grpc: CustomerGrpc
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            grpc.getCustomerPage(
                customerRequestPage {
                    page = 1
                    pageSize = 20
                }
            ).catch { it.message.toString().showShortToast() }
                .collect {
                    _uiState.value = _uiState.value.copy(list = it.listList)
                }
        }
    }

    fun search(search: CustomerSearch) {
        viewModelScope.launch {
            grpc.searchCustomer(search).catch {
                "查询失败".showShortToast()
            }.collect {
                _uiState.value = _uiState.value.copy(list = it.listList)
            }
        }
    }

    fun setCustomer(customer: Customer?) {
        _uiState.value = _uiState.value.copy(customer = customer)
    }

    fun insert(customer: Customer) {
        viewModelScope.launch {
            grpc.add(customer).catch {
                "添加失败".showShortToast()
            }.collect {
                "添加成功".showShortToast()
                val list = _uiState.value.list.toMutableList()
                _uiState.value = _uiState.value.copy(list = list.plus(customer))
            }
        }
    }

    fun update(customer: Customer) {
        viewModelScope.launch {
            grpc.update(customer).catch {
                "更新失败".showShortToast()
            }.collect {
                "更新成功".showShortToast()
                val list = _uiState.value.list.toMutableList()
                list.remove(customer)
                _uiState.value = _uiState.value.copy(list = list.plus(customer))
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

data class CustomerUiState(
    val list: List<Customer> = emptyList(),
    val customer: Customer? = null
)