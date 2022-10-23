@file:Suppress("DEPRECATION")

package com.zktony.www.common.extension

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File

fun Context.versionCode(): Int {
    var versionCode = 0
    try {
        versionCode = this.packageManager.getPackageInfo(this.packageName, 0).versionCode
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return versionCode
}

fun Context.versionName(): String? {
    var verName: String? = ""
    try {
        verName = this.packageManager.getPackageInfo(this.packageName, 0).versionName
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return verName
}

fun Context.installApk(apk: File) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.addCategory(Intent.CATEGORY_DEFAULT)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val type = "application/vnd.android.package-archive"
    val uri: Uri
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        uri = FileProvider.getUriForFile(
            this,
            this.packageName + ".fileProvider",
            apk
        )
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    } else {
        uri = Uri.fromFile(apk)
    }
    intent.setDataAndType(uri, type)
    this.startActivity(intent)
}