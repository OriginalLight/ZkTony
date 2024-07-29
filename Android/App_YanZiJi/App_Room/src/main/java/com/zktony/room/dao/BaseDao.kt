package com.zktony.room.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

/**
 * Base DAO.
 */
interface BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: T): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg entity: T): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: Collection<T>): List<Long>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(entity: T): Int

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAll(vararg entity: T): Int

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAll(entities: Collection<T>): Int

    @Delete
    suspend fun delete(entity: T): Int

    @Delete
    suspend fun deleteAll(vararg entity: T): Int

    @Delete
    suspend fun deleteAll(entities: Collection<T>): Int

}