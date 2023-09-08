package com.zktony.android.utils.ext

import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zktony.android.utils.Constants
import okhttp3.*
import java.io.IOException

/**
 * @author 刘贺贺
 * @date 2023/7/24 9:05
 */

fun httpCall(url: String = Constants.OSS_APP, callback: (List<Application>) -> Unit) {
    val request = Request.Builder().url(url).get().build()
    val call = OkHttpClient.Builder().build().newCall(request)

    call.enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
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