package com.zktony.www.data.local.dao

import androidx.room.*
import com.zktony.common.room.dao.BaseDao
import com.zktony.www.data.local.entity.Program
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao : BaseDao<Program> {

    @Query(
        """
            SELECT * FROM program
            ORDER BY count DESC
        """
    )
    fun getAll(): Flow<List<Program>>

    @Query(
        """
            SELECT * FROM program
            WHERE id = :id
        """
    )
    fun getById(id: String): Flow<Program>

    @Query(
        """
            UPDATE program
            SET def = 0
            WHERE model == :kind
            AND def == 1
        """
    )
    suspend fun updateDefaultByKind(kind: Int)

    @Query(
        """
            SELECT * FROM program
            WHERE upload = 0
        """
    )
    fun withoutUpload(): Flow<List<Program>>
}