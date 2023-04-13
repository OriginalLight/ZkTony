package com.zktony.www.ui.program

import android.graphics.Color
import android.view.View
import android.widget.EditText
import androidx.lifecycle.viewModelScope
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.zktony.core.base.BaseViewModel
import com.zktony.www.R
import com.zktony.www.room.dao.PointDao
import com.zktony.www.room.entity.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProgramPointViewModel constructor(
    private val dao: PointDao
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ProgramHoleUiState())
    val uiState = _uiState.asStateFlow()

    fun init(id: Long, index: Int) {
        viewModelScope.launch {
            dao.getBySudIdByIndex(id, index).collect {
                _uiState.value = _uiState.value.copy(list = it)
            }
        }
    }

    fun selectAll() {
        viewModelScope.launch {
            dao.updateAll(_uiState.value.list.map {
                it.copy(enable = true)
            })
        }
    }

    fun select(x: Int, y: Int) {
        viewModelScope.launch {
            if (!_uiState.value.custom) {
                val point = _uiState.value.list.find { it.x == x && it.y == y }!!
                dao.update(point.copy(enable = !point.enable))
            } else {
                val point = _uiState.value.list.find { it.x == x && it.y == y }!!
                if (point.enable) {
                    dao.update(point.copy(enable = false))
                } else {
                    volumeDialog(point) {
                        viewModelScope.launch {
                            dao.update(it)
                        }
                    }
                }
            }
        }
    }

    fun setVolume() {
        viewModelScope.launch {
            val list = _uiState.value.list
            volumeDialog(point = list[0]) { point ->
                viewModelScope.launch {
                    val list1 = mutableListOf<Point>()
                    list.forEach {
                        list1.add(it.copy(v1 = point.v1, v2 = point.v2, v3 = point.v3, v4 = point.v4))
                    }
                    dao.updateAll(list1)
                }
            }
        }
    }

    fun custom() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(custom = !_uiState.value.custom)
        }
    }

    private fun volumeDialog(point: Point, block: (Point) -> Unit) {
        CustomDialog.build()
            .setCustomView(object :
                OnBindView<CustomDialog>(R.layout.layout_volume) {
                override fun onBind(dialog: CustomDialog, v: View) {
                    val inputV1 = v.findViewById<EditText>(R.id.input_v1)
                    val inputV2 = v.findViewById<EditText>(R.id.input_v2)
                    val inputV3 = v.findViewById<EditText>(R.id.input_v3)
                    val inputV4 = v.findViewById<EditText>(R.id.input_v4)
                    val save = v.findViewById<MaterialButton>(R.id.ok)
                    val cancel = v.findViewById<MaterialButton>(R.id.cancel)
                    if (point.v1 != 0) inputV1.setText(point.v1.toString())
                    if (point.v2 != 0) inputV2.setText(point.v2.toString())
                    if (point.v3 != 0) inputV3.setText(point.v3.toString())
                    if (point.v4 != 0) inputV4.setText(point.v4.toString())

                    save.setOnClickListener {
                        val v1 = inputV1.text.toString().toIntOrNull() ?: 0
                        val v2 = inputV2.text.toString().toIntOrNull() ?: 0
                        val v3 = inputV3.text.toString().toIntOrNull() ?: 0
                        val v4 = inputV4.text.toString().toIntOrNull() ?: 0
                        block(point.copy(v1 = v1, v2 = v2, v3 = v3, v4 = v4, enable = true))
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
}

data class ProgramHoleUiState(
    val list: List<Point> = emptyList(),
    val custom: Boolean = false,
)