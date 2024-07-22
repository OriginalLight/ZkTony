package com.zktony.room.repository

import com.zktony.room.dao.UserDao
import com.zktony.room.defaults.defaultUsers
import com.zktony.room.entities.User
import java.security.MessageDigest
import java.util.Date
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDao: UserDao
) {

    /**
     * Init.
     *
     * Create default factory and admin user.
     */
    suspend fun init() {
        defaultUsers().forEach {
            userDao.getByName(it.name) ?: run {
                val digest = MessageDigest.getInstance("SHA-256")
                val hash = digest.digest(it.password.toByteArray())
                val passwordHash = hash.fold("") { str, it -> str + "%02x".format(it) }
                userDao.insert(it.copy(password = passwordHash))
            }
        }
    }

    /**
     * Login.
     *
     * @param username Username.
     * @param password Password.
     * @return user if success, 001 if user not found, 002 if password incorrect，003 if user disabled, 004 if update failed.
     */
    suspend fun login(username: String, password: String): Result<User> {
        val user = userDao.getByName(username) ?: return Result.failure(Exception("001"))
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        val passwordHash = hash.fold("") { str, it -> str + "%02x".format(it) }
        if (user.password != passwordHash) return Result.failure(Exception("002"))
        if (!user.enable) return Result.failure(Exception("003"))
        val updateUser = user.copy(lastLoginTime = Date(System.currentTimeMillis()))
        val effect = userDao.update(updateUser)
        if (effect == 0) return Result.failure(Exception("004"))
        return Result.success(updateUser)
    }

    /**
     * Register.
     *
     * @param user User.
     * @return user with id if success, 001 if user exists. 002 if failed.
     */
    suspend fun register(user: User): Result<User> {
        userDao.getByName(user.name)?.let { return Result.failure(Exception("001")) }
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(user.password.toByteArray())
        val passwordHash = hash.fold("") { str, it -> str + "%02x".format(it) }
        val id = userDao.insert(user.copy(password = passwordHash))
        return if (id > 0)  Result.success(user.copy(id = id)) else Result.failure(Exception("002"))
    }
}