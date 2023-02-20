package com.zktony.www.common.extension

import android.annotation.SuppressLint
import android.graphics.Color
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.kongzue.dialogx.dialogs.*
import com.kongzue.dialogx.interfaces.OnBindView
import com.kongzue.dialogx.util.TextInfo
import com.zktony.common.app.CommonApplicationProxy
import com.zktony.common.extension.createQRCodeBitmap
import com.zktony.common.extension.isFastClick
import com.zktony.common.extension.isNetworkAvailable
import com.zktony.common.utils.Constants
import com.zktony.www.R
import com.zktony.www.data.remote.model.QrCode

/**
 * @author: 刘贺贺
 * @date: 2023-01-30 9:39
 */


fun deleteDialog(name: String, block: () -> Unit) {
    MessageDialog.show(
        "提示", "确定删除 $name 吗？", "确定", "取消"
    ).setOkButton { _, _ ->
        block()
        false
    }
}

fun noticeDialog() {
    CustomDialog.build()
        .setCustomView(object : OnBindView<CustomDialog>(R.layout.layout_notice_dialog) {
            override fun onBind(dialog: CustomDialog, v: View) {
                val btnOk = v.findViewById<MaterialButton>(R.id.btn_ok)
                btnOk.setOnClickListener {
                    dialog.dismiss()
                }
            }
        }).setMaskColor(Color.parseColor("#4D000000")).setWidth(500).show()
}


fun aboutDialog() {
    CustomDialog.build()
        .setCustomView(object : OnBindView<CustomDialog>(R.layout.layout_about_dialog) {
            override fun onBind(dialog: CustomDialog, v: View) {
                val btnWeb = v.findViewById<MaterialButton>(R.id.btn_web)
                btnWeb.isVisible = CommonApplicationProxy.application.isNetworkAvailable()
                btnWeb.setOnClickListener {
                    if (isFastClick().not()) {
                        dialog.dismiss()
                        webDialog()
                    }
                }
            }
        }).setMaskColor(Color.parseColor("#4D000000")).show()
}

fun deviceDialog() {
    CustomDialog.build()
        .setCustomView(object : OnBindView<CustomDialog>(R.layout.layout_device_dialog) {
            @SuppressLint("HardwareIds")
            override fun onBind(dialog: CustomDialog, v: View) {
                val device = v.findViewById<ImageView>(R.id.device)
                val id = Settings.Secure.getString(
                    CommonApplicationProxy.application.contentResolver, Settings.Secure.ANDROID_ID
                )
                val code = Gson().toJson(QrCode(id = id)).toString()
                val image = createQRCodeBitmap(
                    content = code,
                    width = 500,
                    height = 500,
                    character_set = "UTF-8",
                    error_correction_level = "H",
                    margin = "1",
                    color_black = Color.BLACK,
                    color_white = Color.WHITE
                )
                if (image == null) {
                    PopTip.show("生成二维码失败")
                    dialog.dismiss()
                }
                device.setImageBitmap(image)
            }
        }).setMaskColor(Color.parseColor("#4D000000")).show()
}

fun webDialog() {
    FullScreenDialog.build(object : OnBindView<FullScreenDialog>(R.layout.layout_about_webview) {
        @SuppressLint("SetJavaScriptEnabled")
        override fun onBind(dialog: FullScreenDialog, v: View) {
            val btnClose = v.findViewById<View>(R.id.btn_close)
            val webView = v.findViewById<View>(R.id.webView)
            btnClose.setOnClickListener { dialog.dismiss() }
            (webView as WebView).settings.apply {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                setSupportZoom(true)
                allowFileAccess = true
                javaScriptCanOpenWindowsAutomatically = true
                loadsImagesAutomatically = true
                defaultTextEncodingName = "utf-8"
            }
            webView.webViewClient = object : WebViewClient() {
                @Deprecated("Deprecated in Java")
                override fun shouldOverrideUrlLoading(
                    view: WebView, url: String
                ): Boolean {
                    view.loadUrl(url)
                    return true
                }

                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                }
            }
            webView.loadUrl(Constants.DOMAIN)
        }
    }).setMaxWidth(1920).show()
}

fun updateDialog(title: String, message: String, block: () -> Unit, block1: () -> Unit) {
    CustomDialog.build()
        .setCustomView(object : OnBindView<CustomDialog>(R.layout.layout_update_dialog) {
            override fun onBind(dialog: CustomDialog, v: View) {
                val tvTitle = v.findViewById<TextView>(R.id.title)
                val tvMessage = v.findViewById<TextView>(R.id.message)
                val btnOk = v.findViewById<MaterialButton>(R.id.btn_ok)
                val btnCancel = v.findViewById<MaterialButton>(R.id.btn_cancel)
                tvTitle.text = title
                tvMessage.text = message
                btnOk.setOnClickListener {
                    block()
                    dialog.dismiss()
                }
                btnCancel.setOnClickListener {
                    block1()
                    dialog.dismiss()
                }
            }
        }).setCancelable(false).setMaskColor(Color.parseColor("#4D000000")).show()
}

fun spannerDialog(view: View, menu: List<String>, block: (String, Int) -> Unit) {
    PopMenu.show(view, menu).setOverlayBaseView(false).setMenuTextInfo(TextInfo().apply {
        gravity = Gravity.CENTER
        fontSize = 16
    }).setOnMenuItemClickListener { _, text, index ->
        block(text.toString(), index)
        false
    }.setRadius(0f).alignGravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
}