package com.zktony.room.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer


object JsonUtils {

    inline fun <reified T> toJson(value: T): String {
        val serializer = serializer<T>()
        return Json.encodeToString(serializer, value)
    }

    inline fun <reified T> fromJson(jsonString: String): T {
        val serializer = serializer<T>()
        return Json.decodeFromString(serializer, jsonString)
    }
}