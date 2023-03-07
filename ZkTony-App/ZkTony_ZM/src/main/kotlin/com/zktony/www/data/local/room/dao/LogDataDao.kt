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
import com.zktony.www.data.local.room.entity.LogData
import kotlinx.coroutines.flow.Flow

/**
 * Data access object to query the database.
 */
@Dao
interface LogDataDao : BaseDao<LogData> {


    @Query("DELETE FROM log_data WHERE logId = :id")
    suspend fun deleteByRecordId(id: String)

    @Query("DELETE FROM log_data WHERE julianday('now') - julianday(createTime) >= '180'")
    suspend fun deleteByDate()

    @Query("SELECT * FROM log_data WHERE logId == :id ORDER BY createTime ASC")
    fun getByLogId(id: String): Flow<List<LogData>>

    @Query("DELETE FROM log_data WHERE  julianday('now') - julianday(createTime) <= '1' AND logId IN (SELECT logId FROM log_data GROUP BY logId HAVING count(*) < 12) ")
    suspend fun deleteDataLessThanTen()

    @Query("SELECT * FROM log_data WHERE upload = 0 LIMIT 200")
    fun withoutUpload(): Flow<List<LogData>>
}
