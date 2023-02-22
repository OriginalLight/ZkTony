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

package com.zktony.www.data.local.room.dao

import androidx.room.*
import com.zktony.www.data.local.room.entity.Log
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Data access object to query the database.
 */
@Dao
interface LogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(logRecord: Log)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(log: Log)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateBatch(logRecords: List<Log>)

    @Query("DELETE FROM log WHERE julianday('now') - julianday(createTime) >= '180'")
    suspend fun deleteByDate()

    @Delete
    suspend fun delete(log: Log)

    @Query("SELECT * FROM log ORDER BY createTime DESC LIMIT 20")
    fun getAll(): Flow<List<Log>>

    @Query("SELECT * FROM log WHERE createTime BETWEEN :start AND :end ORDER BY createTime DESC")
    fun getByDate(start: Date, end: Date): Flow<List<Log>>

    @Query("SELECT * FROM log WHERE upload = 0 LIMIT 20")
    fun withoutUpload(): Flow<List<Log>>
}