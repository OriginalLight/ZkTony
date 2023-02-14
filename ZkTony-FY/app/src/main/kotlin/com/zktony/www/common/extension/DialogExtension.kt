package com.zktony.www.common.extension

import android.annotation.SuppressLint
import android.graphics.Color
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.*
import com.kongzue.dialogx.interfaces.OnBindView
import com.kongzue.dialogx.util.InputInfo
import com.kongzue.dialogx.util.TextInfo
import com.zktony.www.BuildConfig
import com.zktony.www.R
import com.zktony.www.common.app.CommonApplicationProxy
import com.zktony.www.common.utils.Constants


fun inputDialog(block: (String) -> Unit) {
    InputDialog("添加", "请输入程序/操作名", "确定", "取消").setCancelable(false)
        .setInputInfo(InputInfo().setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL))
        .setOkButton { _, _, inputStr ->
            if (inputStr.trim().isEmpty()) {
                "程序/操作不能为空".showShortToast()
                return@setOkButton false
            }
            block(inputStr.trim())
            false
        }.show()
}

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

fun authDialog(block: () -> Unit) {
    InputDialog("权限认证", "请输入密码", "确定", "取消")
        .setCancelable(false)
        .setInputInfo(InputInfo().setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD))
        .setOkButton { _, _, inputStr ->
            if (inputStr.isBlank().not() && inputStr == "123456") {
                block()
            } else {
                PopTip.show("密码错误")
            }
            false
        }
        .show()
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
                val image = createQRCodeBitmap(
                    content = "android_id:$id," +
                            "device_name:全自动孵育," +
                            "application_id:${BuildConfig.APPLICATION_ID}," +
                            "version_code:${BuildConfig.VERSION_CODE}," +
                            "version_name:${BuildConfig.VERSION_NAME}," +
                            "build_type:${BuildConfig.BUILD_TYPE}," +
                            "build_type:${BuildConfig.BUILD_TYPE}",
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

fun selectDialog(block: () -> Unit, block1: () -> Unit) {
    CustomDialog.build()
        .setCustomView(object :
            OnBindView<CustomDialog>(R.layout.layout_function_select) {
            override fun onBind(dialog: CustomDialog, v: View) {
                val motor = v.findViewById<MaterialButton>(R.id.motor)
                val container = v.findViewById<MaterialButton>(R.id.container)
                val cancel = v.findViewById<MaterialButton>(R.id.cancel)
                motor.setOnClickListener {
                    block()
                    dialog.dismiss()
                }
                container.setOnClickListener {
                    block1()
                    dialog.dismiss()
                }
                cancel.setOnClickListener { dialog.dismiss() }
            }
        })
        .setMaskColor(Color.parseColor("#4D000000"))
        .show()
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
        })
        .setMaskColor(Color.parseColor("#4D000000"))
        .show()
}

fun webDialog() {
    FullScreenDialog.build(object :
        OnBindView<FullScreenDialog>(R.layout.layout_about_webview) {
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
                    view: WebView,
                    url: String
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
        })
        .setCancelable(false)
        .setMaskColor(Color.parseColor("#4D000000"))
        .show()
}

fun spannerDialog(view: View, menu: List<String>, block: (String, Int) -> Unit) {
    PopMenu.show(view, menu)
        .setOverlayBaseView(false)
        .setMenuTextInfo(TextInfo().apply {
            gravity = Gravity.CENTER
            fontSize = 16
        }).setOnMenuItemClickListener { _, text, index ->
            block(text.toString(), index)
            false
        }
        .setRadius(0f)
        .alignGravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
}