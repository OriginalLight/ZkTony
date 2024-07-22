package com.zktony.android.utils

import com.zktony.android.data.Role
import com.zktony.room.entities.User

object AuthUtils {
    private var loggedUser: User? = null

    fun getRole(): Role {
        return loggedUser?.role?.let { Role.fromName(it) } ?: Role.USER
    }

    fun getLoggedUser(): User? {
        return loggedUser
    }

    fun login(user: User) {
        loggedUser = user
    }

    fun logout() {
        loggedUser = null
    }
}