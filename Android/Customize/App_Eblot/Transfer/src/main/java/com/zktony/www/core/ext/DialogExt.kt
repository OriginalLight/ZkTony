package com.zktony.www.core.ext

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Color
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.FullScreenDialog
import com.kongzue.dialogx.dialogs.PopMenu
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnBindView
import com.kongzue.dialogx.style.MaterialStyle
import com.kongzue.dialogx.util.TextInfo
import com.zktony.core.BuildConfig
import com.zktony.core.ext.Ext
import com.zktony.core.ext.clickNoRepeat
import com.zktony.core.ext.createQRCodeBitmap
import com.zktony.core.ext.isNetworkAvailable
import com.zktony.core.utils.Constants
import com.zktony.www.R

fun initDialogX(application: Application) {

    DialogX.init(application)
    //开启调试模式，在部分情况下会使用 Log 输出日志信息
    DialogX.DEBUGMODE = BuildConfig.DEBUG

    //设置主题样式
    DialogX.globalStyle = MaterialStyle.style()

    //设置亮色/暗色（在启动下一个对话框时生效）
    DialogX.globalTheme = DialogX.THEME.DARK

    //设置 InputDialog 自动弹出键盘
    DialogX.autoShowInputKeyboard = true

}

fun aboutDialog() {
    CustomDialog.build()
        .setCustomView(object : OnBindView<CustomDialog>(R.layout.layout_about_dialog) {
            override fun onBind(dialog: CustomDialog, v: View) {

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