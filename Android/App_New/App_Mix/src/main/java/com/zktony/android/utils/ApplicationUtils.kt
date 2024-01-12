package com.zktony.android.utils

import android.app.Application
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File

/**
 * @author 刘贺贺
 * @date 2023/9/13 8:56
 */
object ApplicationUtils {
    lateinit var ctx: Application

    fun with(app: Application) {
        ctx = app
    }

    fun installApp(apk: File) {
        val type = "application/vnd.android.package-archive"
        val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".fileProvider", apk)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(uri, type)
        }
        ctx.startActivity(intent)
    }


    fun isNetworkAvailable(): Boolean {
        val cm = ctx.getSystemService(Application.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return true
            }
        } else {
            @Suppress("DEPRECATION")
            return cm.activeNetworkInfo?.isAvailable ?: false
        }
        return false
    }
}