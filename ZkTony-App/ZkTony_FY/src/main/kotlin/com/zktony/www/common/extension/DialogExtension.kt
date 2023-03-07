package com.zktony.www.common.extension

import android.graphics.Color
import android.view.View
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.zktony.www.R

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