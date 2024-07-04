package com.zktony.room.repository

import com.zktony.room.dao.UserDao
import com.zktony.room.entities.User
import java.security.MessageDigest
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val dao: UserDao
) {
    /**
     * Login.
     *
     * @param username Username.
     * @param password Password.
     * @return 0 if success, 1 if user not found, 2 if password incorrect.
     */
    fun login(username: String, password: String) : Int {
        val user = dao.getByName(username) ?: return 1
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        val passwordHash = hash.fold("") { str, it -> str + "%02x".format(it) }
        return if (user.password == passwordHash) 0 else 2
    }

    /**
     * Register.
     *
     * @param user User.
     * @return 0 if success, 1 if user exists. 2 if failed.
     */
    suspend fun register(user: User) : Int {
        dao.getByName(user.name)?.let { return 1 }
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(user.password.toByteArray())
        val passwordHash = hash.fold("") { str, it -> str + "%02x".format(it) }
        val id = dao.insert(user.copy(password = passwordHash))
        return if (id > 0) 0 else 2
    }
}