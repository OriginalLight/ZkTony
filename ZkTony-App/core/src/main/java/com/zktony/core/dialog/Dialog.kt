package com.zktony.core.dialog

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.*
import com.kongzue.dialogx.interfaces.OnBindView
import com.kongzue.dialogx.util.TextInfo
import com.zktony.core.R
import com.zktony.core.ext.*
import com.zktony.core.utils.Constants


fun aboutDialog(block: () -> Unit) {
    CustomDialog.build()
        .setCustomView(object : OnBindView<CustomDialog>(R.layout.layout_about_dialog) {
            override fun onBind(dialog: CustomDialog, v: View) {
                val btnWeb = v.findViewById<MaterialButton>(R.id.btn_web)
                btnWeb.isVisible = Ext.ctx.isNetworkAvailable()
                btnWeb.clickNoRepeat {
                    dialog.dismiss()
                    block()
                }
            }
        }).setMaskColor(Color.parseColor("#4D000000")).show()
}

fun authDialog(block: () -> Unit) {
    CustomDialog.build()
        .setCustomView(object : OnBindView<CustomDialog>(R.layout.layout_input) {
            override fun onBind(dialog: CustomDialog, v: View) {
                val tvTitle = v.findViewById<TextView>(R.id.title)
                val etInput = v.findViewById<EditText>(R.id.input)
                val btnOk = v.findViewById<MaterialButton>(R.id.ok)
                val btnCancel = v.findViewById<MaterialButton>(R.id.cancel)
                tvTitle.text = "权限认证"
                etInput.hint = "请输入密码"
                etInput.inputType = InputType.TYPE_CLASS_TEXT
                btnOk.clickNoRepeat {
                    if (etInput.text.isBlank().not() && etInput.text.toString() == "123456") {
                        block()
                        dialog.dismiss()
                    } else {
                        PopTip.show("密码错误")
                    }
                }
                btnCancel.clickNoRepeat {
                    dialog.dismiss()
                }
            }
        })
        .setCancelable(false)
        .setMaskColor(Color.parseColor("#4D000000"))
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

fun inputDialog(
    title: String,
    hint: String = "",
    value: String = "",
    inputType: Int = InputType.TYPE_CLASS_TEXT,
    block: (String) -> Unit
) {
    CustomDialog.build()
        .setCustomView(object : OnBindView<CustomDialog>(R.layout.layout_input) {
            override fun onBind(dialog: CustomDialog, v: View) {
                val tvTitle = v.findViewById<TextView>(R.id.title)
                val etInput = v.findViewById<EditText>(R.id.input)
                val btnOk = v.findViewById<MaterialButton>(R.id.ok)
                val btnCancel = v.findViewById<MaterialButton>(R.id.cancel)
                tvTitle.text = title
                etInput.hint = hint
                etInput.inputType = inputType
                etInput.setText(value)
                btnOk.clickNoRepeat {
                    block(etInput.text.toString())
                    dialog.dismiss()
                }
                btnCancel.clickNoRepeat {
                    dialog.dismiss()
                }
            }
        })
        .setCancelable(false)
        .setMaskColor(Color.parseColor("#4D000000"))
        .show()
}

fun messageDialog(
    title: String,
    message: String = "",
    block: () -> Unit = {}
) {
    CustomDialog.build()
        .setCustomView(object : OnBindView<CustomDialog>(R.layout.layout_message) {
            override fun onBind(dialog: CustomDialog, v: View) {
                val tvTitle = v.findViewById<TextView>(R.id.title)
                val tvMessage = v.findViewById<TextView>(R.id.message)
                val btnOk = v.findViewById<MaterialButton>(R.id.ok)
                val btnCancel = v.findViewById<MaterialButton>(R.id.cancel)
                tvTitle.text = title
                tvMessage.text = message
                btnOk.clickNoRepeat {
                    block()
                    dialog.dismiss()
                }
                btnCancel.clickNoRepeat {
                    dialog.dismiss()
                }
            }
        })
        .setCancelable(false)
        .setMaskColor(Color.parseColor("#4D000000"))
        .show()
}


fun updateDialog(
    title: String,
    message: String,
    block: () -> Unit,
    block1: () -> Unit = {}
) {
    CustomDialog.build()
        .setCustomView(object : OnBindView<CustomDialog>(R.layout.layout_update_dialog) {
            override fun onBind(dialog: CustomDialog, v: View) {
                val tvTitle = v.findViewById<TextView>(R.id.title)
                val tvMessage = v.findViewById<TextView>(R.id.message)
                val btnOk = v.findViewById<MaterialButton>(R.id.btn_ok)
                val btnCancel = v.findViewById<MaterialButton>(R.id.btn_cancel)
                tvTitle.text = title
                tvMessage.text = message
                btnOk.clickNoRepeat {
                    block()
                    dialog.dismiss()
                }
                btnCancel.clickNoRepeat {
                    block1()
                    dialog.dismiss()
                }
            }
        })
        .setCancelable(false)
        .setMaskColor(Color.parseColor("#4D000000"))
        .show()
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
    }.setRadius(0f)
        .alignGravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
}