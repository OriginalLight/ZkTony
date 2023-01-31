/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zktony.www.common.room.dao

import androidx.room.*
import com.zktony.www.common.room.entity.LogRecord
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Data access object to query the database.
 */
@Dao
interface LogRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(logRecord: LogRecord)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(logRecord: LogRecord)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateBatch(logRecords: List<LogRecord>)

    @Delete
    suspend fun delete(logRecord: LogRecord)

    @Query("DELETE FROM log_record WHERE julianday('now') - julianday(createTime) >= '180'")
    suspend fun deleteByDate()

    @Query("DELETE FROM log_record WHERE  julianday('now') - julianday(createTime) <= '1' AND id NOT IN (SELECT da.logId FROM log_data AS da WHERE  julianday('now') - julianday(da.createTime) <= '1')")
    suspend fun deleteInvaliedLog()

    @Query("SELECT * FROM log_record WHERE createTime BETWEEN :start AND :end ORDER BY createTime DESC")
    fun getByDate(start: Date, end: Date): Flow<List<LogRecord>>

    @Query("SELECT * FROM log_record ORDER BY createTime DESC LIMIT 20")
    fun getAll(): Flow<List<LogRecord>>

    @Query("SELECT * FROM log_record WHERE upload = 0 LIMIT 20")
    fun withoutUpload(): Flow<List<LogRecord>>
}
