package com.zktony.www.common.ext

import android.graphics.Color
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.zktony.www.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author: 刘贺贺
 * @date: 2023-01-30 9:39
 */


fun washDialog(block: (Int) -> Unit, block1: () -> Unit) {
    CustomDialog.build()
        .setCustomView(object : OnBindView<CustomDialog>(R.layout.layout_wash) {
            override fun onBind(dialog: CustomDialog, v: View) {
                val input = v.findViewById<EditText>(R.id.input)
                val btnStart = v.findViewById<MaterialButton>(R.id.start)
                val btnStop = v.findViewById<MaterialButton>(R.id.stop)
                val btnCancel = v.findViewById<MaterialButton>(R.id.cancel)
                btnStart.setOnClickListener {
                    val scope = CoroutineScope(Dispatchers.Main)
                    val time = input.text.toString().toIntOrNull() ?: 30
                    block(time)
                    scope.launch {
                        btnStart.isEnabled = false
                        delay(time * 1000L)
                        btnStart.isEnabled = true
                    }
                }
                btnStop.setOnClickListener {
                    block1()
                    btnStart.isEnabled = true
                }
                btnCancel.setOnClickListener {
                    dialog.dismiss()
                }
            }
        })
        .setCancelable(false)
        .setMaskColor(Color.parseColor("#4D000000"))
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
        .setMaskColor(Color.parseColor("#4D000000")).show()
}