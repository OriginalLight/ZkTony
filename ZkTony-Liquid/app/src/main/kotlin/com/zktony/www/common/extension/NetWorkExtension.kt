package com.zktony.www.common.extension

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

/**
 * @author: 刘贺贺
 * @date: 2022-09-26 8:52
 */

/**
 * 网络是否可用
 */
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