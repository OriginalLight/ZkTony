package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zktony.android.data.Role
import com.zktony.android.data.UserQuery
import com.zktony.android.data.defaults.defaultUserQuery
import com.zktony.android.ui.components.Tips
import com.zktony.android.utils.AuthUtils
import com.zktony.android.utils.TipsUtils
import com.zktony.log.LogUtils
import com.zktony.room.entities.User
import com.zktony.room.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsUserManagementViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _selected = MutableStateFlow<List<Long>>(emptyList())
    private val _query = MutableStateFlow(defaultUserQuery())

    val selected = _selected.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val entities = _query.flatMapLatest { query ->
        Pager(
            config = PagingConfig(pageSize = 20, initialLoadSize = 40),
        ) {
            userRepository.getByPage(
                Role.getLowerRoleName(AuthUtils.getRole()),
                query.name
            )
        }.flow.cachedIn(
            viewModelScope
        )
    }

    // 删除
    suspend fun delete() {
        try {
            val ids = _selected.value
            if (ids.isEmpty()) {
                return
            }
            val res = userRepository.deleteByIds(ids)
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

    // 添加
    suspend fun add(obj: User): Int {
        try {
            if (userRepository.insert(obj)) {
                TipsUtils.showTips(Tips.info("添加成功"))
                return 0
            } else {
                TipsUtils.showTips(Tips.error("添加失败"))
            }
        } catch (e: Exception) {
            when (e.message) {
                "1" -> {
                    TipsUtils.showTips(Tips.error("名称已存在"))
                    return 1
                }

                else -> TipsUtils.showTips(Tips.error("添加失败"))
            }
            LogUtils.error(e.stackTraceToString(), true)
        }

        return -1
    }

    // 更新
    suspend fun update(obj: User): Boolean {
        try {
            if (userRepository.update(obj)) {
                TipsUtils.showTips(Tips.info("更新成功"))
                return true
            } else {
                TipsUtils.showTips(Tips.error("更新失败"))
            }
        } catch (e: Exception) {
            when (e.message) {
                "1" -> TipsUtils.showTips(Tips.error("名称已存在"))
                else -> TipsUtils.showTips(Tips.error("更新失败"))
            }
            LogUtils.error(e.stackTraceToString(), true)
        }

        return false
    }

    // 搜索
    fun search(query: UserQuery) {
        _query.value = query
        _selected.value = emptyList()
        TipsUtils.showTips(Tips.info("搜索成功"))
    }

    // 清空密码
    suspend fun clearPassword(user: User) {
        try {
            if (userRepository.modifyPassword(user.id, user.name)) {
                TipsUtils.showTips(Tips.info("清空密码成功"))
            } else {
                TipsUtils.showTips(Tips.error("清空密码失败"))
            }
        } catch (e: Exception) {
            when (e.message) {
                "1" -> TipsUtils.showTips(Tips.error("用户不存在"))
                else -> TipsUtils.showTips(Tips.error("清空密码失败"))
            }
            LogUtils.error(e.stackTraceToString(), true)
        }
    }

}