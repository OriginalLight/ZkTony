package com.zktony.www.common.ext

import android.graphics.Color
import android.view.View
import android.widget.TextView
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.zktony.www.R

/**
 * @author: 刘贺贺
 * @date: 2023-01-30 9:39
 */


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