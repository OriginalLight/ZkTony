package com.zktony.manager.data.store

import com.zktony.manager.data.local.dao.UserDao
import com.zktony.manager.data.local.model.User

/**
 * @author: 刘贺贺
 * @date: 2023-02-16 10:33
 */
class UserStore(
    private val dao: UserDao
) {
    fun getAll() = dao.getAll()
    fun get() = dao.get()
    suspend fun insert(user: User) = dao.insert(user)
    suspend fun update(user: User) = dao.update(user)
    suspend fun delete(user: User) = dao.delete(user)
}