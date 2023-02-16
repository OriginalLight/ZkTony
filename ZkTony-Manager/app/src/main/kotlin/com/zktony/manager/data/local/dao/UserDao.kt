package com.zktony.manager.data.local.dao

import androidx.room.*
import com.zktony.manager.data.local.model.User
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2023-02-16 10:11
 */
@Dao
interface UserDao {

    @Query(
        """
        SELECT * FROM user
        """
    )
    fun getAll(): Flow<List<User>>

    @Query(
        """
        SELECT * FROM user
        LIMIT 1
        """
    )
    fun get(): Flow<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)
}