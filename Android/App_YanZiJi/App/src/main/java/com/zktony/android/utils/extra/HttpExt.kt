package com.zktony.android.utils.extra

import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zktony.android.BuildConfig
import com.zktony.android.utils.Constants
import okhttp3.*
import java.io.IOException

/**
 * @author 刘贺贺
 * @date 2023/7/24 9:05
 */

fun httpCall(
    url: String = Constants.OSS_APP,
    exception: (Exception) -> Unit = {},
    callback: (Application?) -> Unit
) {
    val request = Request.Builder().url(url).get().build()
    val call = OkHttpClient.Builder().build().newCall(request)

    call.enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            exception(e)
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                try {
                    val resp = response.body.string()
                    val type = object : TypeToken<List<Application>>() {}.type
                    val list = Gson().fromJson<List<Application>>(resp, type)
                    callback(list.find { app -> app.applicationId == BuildConfig.APPLICATION_ID })
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    exception(ex)
                }
            } else {
                exception(Exception("请求失败"))
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
    val downloadUrl: String
)