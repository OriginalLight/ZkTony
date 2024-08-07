package com.zktony.core.ext

import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zktony.core.utils.Constants
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * @author 刘贺贺
 * @date 2023/7/24 9:05
 */

fun httpCall(url: String = Constants.OSS_APP, failure: () -> Unit, callback: (List<Application>) -> Unit) {
    val request = Request.Builder().url(url).get().build()
    val call = OkHttpClient.Builder().build().newCall(request)

    call.enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            failure()
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                try {
                    val resp = response.body.string()
                    val type = object : TypeToken<List<Application>>() {}.type
                    val list = Gson().fromJson<List<Application>>(resp, type)
                    callback(list)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                throw Exception(response.toString())
            }
        }
    })
}

@Keep
data class Application(
    val id: Int,
    val applicationId: String,
    val buildType: String,
    val versionCode: Int,
    val versionName: String,
    val createTime: String,
    val description: String,
    val downloadUrl: String,
)