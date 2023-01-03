package com.zktony.www.data.dao

import androidx.room.*
import com.zktony.www.data.model.Program
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(program: Program)

    @Query("SELECT * FROM program ORDER BY count DESC")
    fun getAll(): Flow<List<Program>>

    @Query("UPDATE program SET def = 0 WHERE model == :kind AND def == 1")
    suspend fun updateDefaultByKind(kind: Int)

    @Delete
    suspend fun delete(program: Program)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(program: Program)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateBatch(programs: List<Program>)

    @Query("SELECT * FROM program WHERE upload = 0")
    fun withoutUpload(): Flow<List<Program>>
}