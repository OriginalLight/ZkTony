package com.zktony.room.repository

import androidx.paging.PagingSource
import com.zktony.room.dao.UserDao
import com.zktony.room.defaults.defaultUsers
import com.zktony.room.entities.User
import java.security.MessageDigest
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
            userDao.getByName(it.name).let { ul ->
                if (ul.isEmpty()) {
                    val digest = MessageDigest.getInstance("SHA-256")
                    val hash = digest.digest(it.password.toByteArray())
                    val passwordHash = hash.fold("") { str, it -> str + "%02x".format(it) }
                    userDao.insert(it.copy(password = passwordHash))
                }
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
    suspend fun login(username: String, password: String): User {
        val ul = userDao.getByName(username)
        if (ul.isEmpty()) error(1)
        val user = ul.first()
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        val passwordHash = hash.fold("") { str, it -> str + "%02x".format(it) }
        if (user.password != passwordHash) error(2)
        if (!user.enable) error(3)
        val updateUser = user.copy(lastLoginTime = System.currentTimeMillis())
        val effect = userDao.update(updateUser)
        if (effect <= 0) error(4)
        return updateUser
    }

    /**
     * Add.
     *
     * @param user User.
     * @return user with id if success, 1 if user exists.
     */
    suspend fun insert(user: User): Boolean {
        val ul = userDao.getByName(user.name)
        if (ul.isNotEmpty()) error(1)
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(user.password.toByteArray())
        val passwordHash = hash.fold("") { str, it -> str + "%02x".format(it) }
        val id = userDao.insert(user.copy(password = passwordHash))
        return id > 0
    }

    /**
     * Update.
     *
     * @param user User.
     * @return bool if success, 1 user exists.
     */
    suspend fun update(user: User): Boolean {
        val ul = userDao.getByName(user.name)
        if (ul.any { u -> u.id != user.id }) error(1)
        val effect = userDao.update(user)
        return effect > 0
    }

    /**
     * Delete.
     *
     * @param ids List<Long>.
     * @return effect if success.
     */
    suspend fun deleteByIds(ids: List<Long>): Boolean {
        val effect = userDao.deleteByIds(ids)
        return effect > 0
    }

    /**
     * Get by page.
     * @param roles List<String>.
     * @return PagingSource<Int, User>.
     */
    fun getByPage(roles: List<String>, name: String?): PagingSource<Int, User> {
        return userDao.getByPage(roles, name)
    }

    /**
     *  Verify password.
     *  @param identity Long.
     *  @param password String.
     *  @return ok if success, 1 if user not found.
     */
    suspend fun verifyPassword(identity: Long, password: String): Boolean {
        val user = userDao.getById(identity) ?: error(1)
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        val passwordHash = hash.fold("") { str, it -> str + "%02x".format(it) }
        return user.password == passwordHash
    }

    /**
     * Modify password.
     * @param identity Long.
     * @param newPassword String.
     * @return ok if success, 1 if user not found.
     */
    suspend fun modifyPassword(identity: Long, newPassword: String): Boolean {
        val user = userDao.getById(identity) ?: error(1)
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(newPassword.toByteArray())
        val passwordHash = hash.fold("") { str, it -> str + "%02x".format(it) }
        val effect = userDao.update(user.copy(password = passwordHash))
        return effect > 0
    }
}