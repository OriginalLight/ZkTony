package com.zktony.www.common.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "program")
class Program(
    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
) {
    constructor() : this(UUID.randomUUID().toString())

    var name = ""
    var motor = 0
    var voltage = 0f
    var time = 0f
    var count = 0
    var thickness = "0.075"
    var glueType = 0
    var glueConcentration = 0f
    var glueMaxConcentration = 0f
    var glueMinConcentration = 0f
    var proteinMaxSize = 0f
    var proteinMinSize = 0f
    var proteinName = ""
    var bufferType = "厂家"
    var model = 0
    var status = 0
    var def = 0
    var upload = 0
    var createTime = Date(System.currentTimeMillis())
}