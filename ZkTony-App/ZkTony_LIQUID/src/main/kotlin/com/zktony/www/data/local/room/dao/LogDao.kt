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
interface LogDao : BaseDao<Log> {
    @Query("DELETE FROM log WHERE julianday('now') - julianday(createTime) >= '180'")
    suspend fun deleteByDate()

    @Query("SELECT * FROM log ORDER BY createTime DESC LIMIT 40")
    fun getAll(): Flow<List<Log>>
}
