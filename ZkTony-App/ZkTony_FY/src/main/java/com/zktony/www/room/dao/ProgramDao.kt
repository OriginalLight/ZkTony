package com.zktony.www.room.dao

import androidx.room.*
import com.zktony.common.room.dao.BaseDao
import com.zktony.www.room.entity.Program
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
    fun getById(id: String): Flow<Program>

    @Query(
        """
        SELECT * FROM program
        WHERE upload = 0
        """
    )
    fun withoutUpload(): Flow<List<Program>>

    @Query(
        """
        DELETE FROM program
        WHERE id = :programId
        """
    )
    suspend fun deleteById(programId: String)
}