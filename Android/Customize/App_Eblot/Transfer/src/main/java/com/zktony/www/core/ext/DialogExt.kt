package com.zktony.www.core.ext

import android.app.Application
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.PopMenu
import com.kongzue.dialogx.interfaces.OnBindView
import com.kongzue.dialogx.style.MaterialStyle
import com.kongzue.dialogx.util.TextInfo
import com.zktony.core.BuildConfig
import com.zktony.core.ext.clickNoRepeat
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
                val btnOk = v.findViewById<Button>(R.id.btn_ok)
                content.text = text
                btnOk.setOnClickListener {
                    dialog.dismiss()
                }
            }
        }).setMaskColor(Color.parseColor("#4D000000")).show()
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
                val btnOk = v.findViewById<Button>(R.id.ok)
                val btnCancel = v.findViewById<Button>(R.id.cancel)
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
                val btnOk = v.findViewById<Button>(R.id.btn_ok)
                val btnCancel = v.findViewById<Button>(R.id.btn_cancel)
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