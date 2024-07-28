package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zktony.android.data.Role
import com.zktony.android.utils.AuthUtils
import com.zktony.room.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsUserManagementViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    val entities = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 40),
    ) { userRepository.getByPage(Role.getLowerRole(AuthUtils.getRole())) }.flow.cachedIn(
        viewModelScope
    )


}