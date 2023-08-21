package com.zktony.android.utils.extra

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.*
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File

/**
 * 安装apk
 * @param apk apk文件
 */
fun Context.installApk(apk: File) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.addCategory(Intent.CATEGORY_DEFAULT)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val type = "application/vnd.android.package-archive"
    val uri: Uri = FileProvider.getUriForFile(
        this,
        this.packageName + ".fileProvider",
        apk
    )
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intent.setDataAndType(uri, type)
    this.startActivity(intent)
}

/**
 * 网络是否可用
 * @return Boolean
 */
@SuppressLint("MissingPermission")
fun Context.isNetworkAvailable(): Boolean {
    val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
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