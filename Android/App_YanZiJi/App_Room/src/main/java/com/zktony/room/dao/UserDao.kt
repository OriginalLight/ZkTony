package com.zktony.room.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.zktony.room.entities.User

@Dao
abstract class UserDao : BaseDao<User> {

    @Query(
        """
        SELECT * FROM users WHERE name = :name
        """
    )
    abstract suspend fun getByName(name: String): User?

    @Query(
        """
        SELECT * FROM users WHERE id = :id
        """
    )
    abstract suspend fun getById(id: Long): User?

    @Query(
        """
        SELECT * FROM users
        WHERE role IN (:roles)
        ORDER BY createTime DESC
        """
    )
    abstract fun getByPage(roles: List<String>): PagingSource<Int, User>
}