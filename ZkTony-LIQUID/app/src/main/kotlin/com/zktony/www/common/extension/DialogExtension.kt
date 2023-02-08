package com.zktony.www.common.extension

import android.graphics.Color
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.zktony.www.R

/**
 * @author: 刘贺贺
 * @date: 2023-01-30 9:39
 */

fun showPositionDialog(
    textX: Float,
    textY: Float,
    block1: (Float, Float) -> Unit,
    block2: (Float, Float) -> Unit
) {
    CustomDialog.build()
        .setCustomView(object :
            OnBindView<CustomDialog>(R.layout.layout_position_input_dialog) {
            override fun onBind(dialog: CustomDialog, v: View) {
                val title = v.findViewById<TextView>(R.id.title)
                val inputX = v.findViewById<EditText>(R.id.input_x)
                val inputY = v.findViewById<EditText>(R.id.input_y)
                val move = v.findViewById<MaterialButton>(R.id.move)
                val save = v.findViewById<MaterialButton>(R.id.save)
                val cancel = v.findViewById<MaterialButton>(R.id.cancel)
                title.text = "设置坐标"
                inputX.setText(textX.toString().removeZero())
                inputY.setText(textY.toString().removeZero())
                move.setOnClickListener {
                    val x = inputX.text.toString().toFloatOrNull() ?: 0f
                    val y = inputY.text.toString().toFloatOrNull() ?: 0f
                    if (isFastClick().not()) {
                        block1(x, y)
                    }
                }
                save.setOnClickListener {
                    val x = inputX.text.toString().toFloatOrNull() ?: 0f
                    val y = inputY.text.toString().toFloatOrNull() ?: 0f
                    block2(x, y)
                    dialog.dismiss()
                }
                cancel.setOnClickListener { dialog.dismiss() }
            }
        })
        .setCancelable(false)
        .setMaskColor(Color.parseColor("#4D000000"))
        .show()
}

fun showSizeDialog(textRow: Int, textColumn: Int, block1: (Int, Int) -> Unit) {
    CustomDialog.build()
        .setCustomView(object :
            OnBindView<CustomDialog>(R.layout.layout_size_input_dialog) {
            override fun onBind(dialog: CustomDialog, v: View) {
                val title = v.findViewById<TextView>(R.id.title)
                val row = v.findViewById<EditText>(R.id.row)
                val column = v.findViewById<EditText>(R.id.column)
                val save = v.findViewById<MaterialButton>(R.id.save)
                val cancel = v.findViewById<MaterialButton>(R.id.cancel)
                title.text = "设置孔板规格"
                row.setText(textRow.toString())
                column.setText(textColumn.toString())
                save.setOnClickListener {
                    val x = row.text.toString().toIntOrNull() ?: 4
                    val y = column.text.toString().toIntOrNull() ?: 4
                    block1(x, y)
                    dialog.dismiss()
                }
                cancel.setOnClickListener { dialog.dismiss() }
            }
        })
        .setCancelable(false)
        .setMaskColor(Color.parseColor("#4D000000"))
        .show()
}

fun showVolumeDialog(
    v1: Float,
    v2: Float,
    v3: Float,
    v4: Float,
    block: (Float, Float, Float, Float) -> Unit,
) {
    CustomDialog.build()
        .setCustomView(object :
            OnBindView<CustomDialog>(R.layout.layout_volume_input_dialog) {
            override fun onBind(dialog: CustomDialog, v: View) {
                val inputV1 = v.findViewById<EditText>(R.id.input_v1)
                val inputV2 = v.findViewById<EditText>(R.id.input_v2)
                val inputV3 = v.findViewById<EditText>(R.id.input_v3)
                val inputV4 = v.findViewById<EditText>(R.id.input_v4)
                val save = v.findViewById<MaterialButton>(R.id.save)
                val cancel = v.findViewById<MaterialButton>(R.id.cancel)
                if (v1 != 0f) inputV1.setText(v1.toString().removeZero())
                if (v2 != 0f) inputV2.setText(v2.toString().removeZero())
                if (v3 != 0f) inputV3.setText(v3.toString().removeZero())
                if (v4 != 0f) inputV4.setText(v4.toString().removeZero())

                save.setOnClickListener {
                    block(
                        inputV1.text.toString().toFloatOrNull() ?: 0f,
                        inputV2.text.toString().toFloatOrNull() ?: 0f,
                        inputV3.text.toString().toFloatOrNull() ?: 0f,
                        inputV4.text.toString().toFloatOrNull() ?: 0f
                    )
                    dialog.dismiss()
                }
                cancel.setOnClickListener { dialog.dismiss() }
            }
        })
        .setCancelable(false)
        .setMaskColor(Color.parseColor("#4D000000"))
        .setWidth(600)
        .show()
}

fun showNotice() {
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