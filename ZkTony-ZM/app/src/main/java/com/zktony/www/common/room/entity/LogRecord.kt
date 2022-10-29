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

package com.zktony.www.common.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

/**
 * Data class that represent the a table in the database.
 */
@Entity(tableName = "log_record")
data class LogRecord(
    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
) {
    constructor() : this(UUID.randomUUID().toString())

    var programId = ""
    var model = 0
    var motor = 0
    var voltage = 0f
    var time = 0f
    var upload = 0
    var createTime: Date = Date(System.currentTimeMillis())
}