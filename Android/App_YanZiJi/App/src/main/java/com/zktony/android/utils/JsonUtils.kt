package com.zktony.android.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer


object JsonUtils {

    // Convert object to json string
    inline fun <reified T> toJson(value: T): String {
        val serializer = serializer<T>()
        return Json.encodeToString(serializer, value)
    }

    // Convert json string to object
    inline fun <reified T> fromJson(jsonString: String): T {
        val serializer = serializer<T>()
        return Json.decodeFromString(serializer, jsonString)
    }
}