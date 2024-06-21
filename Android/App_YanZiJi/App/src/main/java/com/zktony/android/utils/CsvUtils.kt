package com.zktony.android.utils

import com.zktony.android.utils.extra.dateFormat
import com.zktony.room.entities.User
import java.io.File


object CsvUtils {

    // 写csv文件
    fun writeCsv() {
        var users = listOf(
            User(1, "test1", "asdsda"), User(2, "test2", "asdsda"), User(3, "test3", "asdsda")
        )
        val csvFile = File(StorageUtils.getCacheDir(), "user.csv")
        csvFile.writeText("id,name,password, createdTime\n")
        users.forEach {
            csvFile.appendText("${it.id},${it.name},${it.password},${it.createTime.dateFormat("yyyy-MM-dd HH-mm-ss")}\n")
        }
    }
}