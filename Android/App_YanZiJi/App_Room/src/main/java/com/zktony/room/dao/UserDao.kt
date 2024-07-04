package com.zktony.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.zktony.room.entities.User

@Dao
abstract class UserDao: BaseDao<User> {

    @Query(
        """
        SELECT * FROM users WHERE name = :name
        """
    )
    abstract suspend fun getByName(name: String): User?
}