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
     * @return user if success, 1 if user not found, 2 if password incorrect，3 if user disabled, 4 if update failed.
     */
    suspend fun login(username: String, password: String): Result<User> {
        val user = userDao.getByName(username) ?: return Result.failure(Exception("1"))
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        val passwordHash = hash.fold("") { str, it -> str + "%02x".format(it) }
        if (user.password != passwordHash) return Result.failure(Exception("2"))
        if (!user.enable) return Result.failure(Exception("3"))
        val updateUser = user.copy(lastLoginTime = Date(System.currentTimeMillis()))
        val effect = userDao.update(updateUser)
        if (effect == 0) return Result.failure(Exception("4"))
        return Result.success(updateUser)
    }

    /**
     * Add.
     *
     * @param user User.
     * @return user with id if success, 1 if user exists. 2 if failed.
     */
    suspend fun insert(user: User): Result<User> {
        userDao.getByName(user.name)?.let { return Result.failure(Exception("1")) }
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(user.password.toByteArray())
        val passwordHash = hash.fold("") { str, it -> str + "%02x".format(it) }
        val id = userDao.insert(user.copy(password = passwordHash))
        return if (id > 0)  Result.success(user.copy(id = id)) else Result.failure(Exception("2"))
    }

    /**
     * Update.
     *
     * @param user User.
     * @return user if success, 1 if user not found, 2 if failed.
     */
    suspend fun update(user: User): Result<User> {
        userDao.getById(user.id)?.let { return Result.failure(Exception("1")) }
        val effect = userDao.update(user)
        return if (effect > 0) Result.success(user) else Result.failure(Exception("2"))
    }

    /**
     * Delete.
     *
     * @param users List<User>.
     * @return List<Long> if success, 1 if failed.
     */
    suspend fun delete(users: List<User>): Result<List<Long>> {
        val effect = userDao.deleteAll(users)
        return if (effect > 0) Result.success(users.map { it.id }) else Result.failure(Exception("1"))
    }

    /**
     * Get by page.
     */
    fun getByPage(roles: List<String>) = userDao.getByPage(roles)

    /**
     *  Verify password.
     */
    suspend fun verifyPassword(identity: Long, password: String): Result<Boolean> {
        val user = userDao.getById(identity) ?: return Result.failure(Exception("1"))
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        val passwordHash = hash.fold("") { str, it -> str + "%02x".format(it) }
        return Result.success(user.password == passwordHash)
    }

    /**
     * Modify password.
     */
    suspend fun modifyPassword(identity: Long, newPassword: String): Result<Boolean> {
        val user = userDao.getById(identity) ?: return Result.failure(Exception("1"))
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(newPassword.toByteArray())
        val passwordHash = hash.fold("") { str, it -> str + "%02x".format(it) }
        val effect = userDao.update(user.copy(password = passwordHash))
        return if (effect > 0) Result.success(true) else Result.failure(Exception("2"))
    }
}