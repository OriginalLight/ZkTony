package com.zktony.www.data.dao

import androidx.room.*
import com.zktony.www.data.entities.Program
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao : BaseDao<Program> {

    @Query(
        """
        SELECT * FROM program
        ORDER BY runCount DESC
        """
    )
    fun getAll(): Flow<List<Program>>

    @Query(
        """
        SELECT * FROM program
        WHERE name = :name
        """
    )
    fun getByName(name: String): Flow<Program>

    @Query(
        """
        SELECT * FROM program
        WHERE id = :id
        """
    )
    fun getById(id: Long): Flow<Program>

    @Query(
        """
        DELETE FROM program
        WHERE id = :id
        """
    )
    suspend fun deleteById(id: Long)
}