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

package com.zktony.www.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Data class that represent the a table in the database.
 */
@Entity(tableName = "log")
data class Log(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val content: String = "",
    val status: Int = 0,
    val upload: Int = 0,
    val createTime: Date = Date(System.currentTimeMillis())
)
