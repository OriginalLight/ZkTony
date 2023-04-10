package com.zktony.www.common.ext

import android.graphics.Color
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.InputDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnBindView
import com.kongzue.dialogx.util.InputInfo
import com.zktony.common.ext.removeZero
import com.zktony.www.R

/**
 * @author: 刘贺贺
 * @date: 2023-01-30 9:39
 */

fun volumeDialog(
    v1: Float,
    v2: Float,
    block: (Float, Float) -> Unit,
) {
    CustomDialog.build()
        .setCustomView(object :
            OnBindView<CustomDialog>(R.layout.layout_volume_input_dialog) {
            override fun onBind(dialog: CustomDialog, v: View) {
                val inputV1 = v.findViewById<EditText>(R.id.input_v1)
                val inputV2 = v.findViewById<EditText>(R.id.input_v2)
                val save = v.findViewById<MaterialButton>(R.id.save)
                val cancel = v.findViewById<MaterialButton>(R.id.cancel)
                if (v1 != 0f) inputV1.setText(v1.toString().removeZero())
                if (v2 != 0f) inputV2.setText(v2.toString().removeZero())

                save.setOnClickListener {
                    block(
                        inputV1.text.toString().toFloatOrNull() ?: 0f,
                        inputV2.text.toString().toFloatOrNull() ?: 0f,
                    )
                    dialog.dismiss()
                }
                cancel.setOnClickListener { dialog.dismiss() }
            }
        })
        .setCancelable(false)
        .setMaskColor(Color.parseColor("#4D000000"))
        .setWidth(500)
        .show()
}

fun completeDialog(name: String, time: String, speed: String) {
    CustomDialog.build()
        .setCustomView(object : OnBindView<CustomDialog>(R.layout.layout_complete) {
            override fun onBind(dialog: CustomDialog, v: View) {
                val tvName = v.findViewById<TextView>(R.id.name)
                val tvTime = v.findViewById<TextView>(R.id.time)
                val tvSpeed = v.findViewById<TextView>(R.id.speed)
                tvSpeed.text = speed
                tvTime.text = time
                tvName.text = name
            }
        })
        .setMaskColor(Color.parseColor("#4D000000")).setWidth(600).show()
}