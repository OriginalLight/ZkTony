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
    abstract suspend fun getByName(name: String): List<User>

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

    @Query(
        """
        DELETE FROM users WHERE id IN (:ids)
        """
    )
    abstract suspend fun deleteByIds(ids: List<Long>): Int
}