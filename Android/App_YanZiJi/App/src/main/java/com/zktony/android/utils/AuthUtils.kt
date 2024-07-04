package com.zktony.android.utils

import com.zktony.room.entities.User

object AuthUtils {
    private var identity: Long = 0
    private var role: Int = 100

    fun getIdentity(): Long {
        return identity
    }

    fun getRole(): Int {
        return role
    }

    fun isLogin(): Boolean {
        return identity != 0L
    }

    fun isFactory(): Boolean {
        return role == 0
    }

    fun isAdmin(): Boolean {
        return role == 1
    }

    fun login(user: User) {
        identity = user.id
        role = user.role
    }

    fun logout() {
        identity = 0
        role = 100
    }
}