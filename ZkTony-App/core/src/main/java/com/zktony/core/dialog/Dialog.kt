package com.zktony.core.dialog

import android.annotation.SuppressLint
import android.graphics.Color
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
import com.zktony.core.R
import com.zktony.core.ext.*
import com.zktony.core.utils.Constants


fun aboutDialog() {
    CustomDialog.build()
        .setCustomView(object : OnBindView<CustomDialog>(R.layout.layout_about_dialog) {
            override fun onBind(dialog: CustomDialog, v: View) {
                val btnWeb = v.findViewById<MaterialButton>(R.id.btn_web)
                btnWeb.isVisible = Ext.ctx.isNetworkAvailable()
                btnWeb.clickNoRepeat {
                    dialog.dismiss()
                    webDialog()
                }
            }
        }).setMaskColor(Color.parseColor("#4D000000")).show()
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

fun deviceDialog(code: String) {
    CustomDialog.build()
        .setCustomView(object : OnBindView<CustomDialog>(R.layout.layout_device_dialog) {
            @SuppressLint("HardwareIds")
            override fun onBind(dialog: CustomDialog, v: View) {
                val device = v.findViewById<ImageView>(R.id.device)
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

fun deleteDialog(name: String, block: () -> Unit) {
    MessageDialog.show(
        "提示", "确定删除 $name 吗？", "确定", "取消"
    ).setOkButton { _, _ ->
        block()
        false
    }
}

fun noticeDialog(text: String) {
    CustomDialog.build()
        .setCustomView(object : OnBindView<CustomDialog>(R.layout.layout_notice_dialog) {
            override fun onBind(dialog: CustomDialog, v: View) {
                val content = v.findViewById<TextView>(R.id.content)
                val btnOk = v.findViewById<MaterialButton>(R.id.btn_ok)
                content.text = text
                btnOk.setOnClickListener {
                    dialog.dismiss()
                }
            }
        }).setMaskColor(Color.parseColor("#4D000000")).setWidth(500).show()
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

fun inputDialog(message: String = "请输入程序/操作名", block: (String) -> Unit) {
    InputDialog("添加", message, "确定", "取消").setCancelable(false)
        .setInputInfo(InputInfo().setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL))
        .setOkButton { _, _, inputStr ->
            if (inputStr.trim().isEmpty()) {
                PopTip.show("不能为空")
                return@setOkButton false
            }
            block(inputStr.trim())
            false
        }.show()
}

fun inputNumberDialog(message: String = "请输入程序/操作名", value: Int, block: (Int) -> Unit) {
    InputDialog("修改", message, "确定", "取消", value.toString())
        .setInputInfo(InputInfo().setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL))
        .setOkButton { _, _, inputStr ->
            if (inputStr.trim().isEmpty()) {
                PopTip.show("不能为空")
                return@setOkButton false
            }
            "test".logi()
            block(inputStr.trim().toIntOrNull() ?: 0)
            false
        }.show()
}

fun inputNumberDialog(message: String = "请输入程序/操作名", value: Float, block: (Float) -> Unit) {
    InputDialog("修改", message, "确定", "取消", value.removeZero())
        .setInputInfo(InputInfo().setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL))
        .setOkButton { _, _, inputStr ->
            if (inputStr.trim().isEmpty()) {
                PopTip.show("不能为空")
                return@setOkButton false
            }
            block(inputStr.trim().toFloatOrNull() ?: 0f)
            false
        }.show()
}

fun inputDecimalDialog(
    message: String = "请输入程序/操作名",
    value: Float,
    move: (Float) -> Unit,
    block: (Float) -> Unit
) {
    InputDialog("修改", message, "确定", "取消", value.removeZero())
        .setInputInfo(InputInfo().setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL))
        .setOtherButton("移动") { _, _, str ->
            move(str.toFloatOrNull() ?: 0f)
            true
        }
        .setOkButton { _, _, inputStr ->
            if (inputStr.trim().isEmpty()) {
                PopTip.show("不能为空")
                return@setOkButton false
            }
            block(inputStr.trim().toFloatOrNull() ?: 0f)
            false
        }.show()
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

fun spannerDialog(
    view: View,
    font: Int = 16,
    menu: List<String>,
    block: (String, Int) -> Unit
) {
    PopMenu.show(view, menu).setOverlayBaseView(false).setMenuTextInfo(TextInfo().apply {
        gravity = Gravity.CENTER
        fontSize = font
    }).setOnMenuItemClickListener { _, text, index ->
        block(text.toString(), index)
        false
    }.setRadius(0f).alignGravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
}