package com.zktony.www.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnBindView
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.clickNoRepeat
import com.zktony.core.ext.format
import com.zktony.core.utils.Constants
import com.zktony.www.data.entities.Program
import com.zktony.www.R
import com.zktony.www.adapter.ProgramAdapter
import com.zktony.www.core.ext.messageDialog
import com.zktony.www.core.ext.spannerDialog
import com.zktony.www.databinding.FragmentProgramBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProgramFragment :
    BaseFragment<ProgramViewModel, FragmentProgramBinding>(R.layout.fragment_program) {

    override val viewModel: ProgramViewModel by viewModel()

    private val adapter by lazy { ProgramAdapter() }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()
    }

    /**
     * 初始化观察者
     */
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    adapter.submitList(it.list)
                    binding.apply {
                        edit.isEnabled = it.selected != null && adapter.selected != null
                        delete.isEnabled = it.selected != null && adapter.selected != null
                    }
                }
            }
        }
    }

    /**
     * 初始化循环列表
     */
    private fun initView() {
        binding.apply {
            recyclerView.adapter = adapter
            adapter.callback = { viewModel.select(it) }
            adapter.onDoubleClick = {
                viewModel.select(it)
                showProgramDialog(it)
            }
            delete.clickNoRepeat {
                messageDialog(
                    title = getString(R.string.delete_program),
                    message = getString(R.string.confirm_delete),
                    block = {
                        viewModel.delete(adapter.selected!!)
                        adapter.selected = null
                        viewModel.select(null)
                    },
                )
            }
            add.clickNoRepeat {
                showProgramDialog(null)
            }
            edit.clickNoRepeat {
                showProgramDialog(adapter.selected)
            }
        }
    }

    private fun showProgramDialog(program: Program?) {
        CustomDialog.build()
            .setCustomView(object : OnBindView<CustomDialog>(R.layout.layout_program) {
                @SuppressLint("SetTextI18n")
                override fun onBind(dialog: CustomDialog, v: View) {
                    val title = v.findViewById<TextView>(R.id.title)
                    val etName = v.findViewById<EditText>(R.id.name)
                    val btnMode = v.findViewById<Button>(R.id.mode)
                    val etVoltage = v.findViewById<EditText>(R.id.voltage)
                    val tvVoltage = v.findViewById<TextView>(R.id.voltage_title)
                    val etTime = v.findViewById<EditText>(R.id.time)
                    val etSpeed = v.findViewById<EditText>(R.id.speed)
                    val layoutSpeed = v.findViewById<LinearLayout>(R.id.layout_speed)
                    val btnOk = v.findViewById<Button>(R.id.ok)
                    val btnCancel = v.findViewById<Button>(R.id.cancel)

                    if (program != null) {
                        title.text = getString(R.string.edit_the_program)
                        etName.setText(program.name)
                        btnMode.text = if (program.model == 0) getString(R.string.transfer) else getString(R.string.stain)
                        etVoltage.setText(program.voltage.format())
                        etTime.setText(program.time.format())
                        etSpeed.setText(program.motor.toString())
                        if (program.model == 0) {
                            layoutSpeed.visibility = View.VISIBLE
                        } else {
                            layoutSpeed.visibility = View.GONE
                        }
                    } else {
                        title.text = getString(R.string.add_the_program)
                    }

                    btnMode.clickNoRepeat {
                        spannerDialog(view = it, menu = listOf(getString(R.string.transfer),getString(R.string.stain))) { text, _ ->
                            btnMode.text = text
                            if (text == getString(R.string.transfer)) {
                                layoutSpeed.visibility = View.VISIBLE
                                etTime.imeOptions =  EditorInfo.IME_ACTION_NEXT
                                tvVoltage.text = "${getString(R.string.transfer_voltage_e)} (0-${Constants.MAX_VOLTAGE_ZM.format()})"
                            } else {
                                layoutSpeed.visibility = View.GONE
                                etTime.imeOptions =  EditorInfo.IME_ACTION_DONE
                                tvVoltage.text = "${getString(R.string.stain_voltage)} (0-${Constants.MAX_VOLTAGE_RS.format()})"
                            }
                        }
                    }

                    btnOk.clickNoRepeat {
                        val name = etName.text.toString()
                        val mode = if (btnMode.text == getString(R.string.transfer)) 0 else 1
                        val voltage = etVoltage.text.toString().toFloatOrNull() ?: 0f
                        val time = etTime.text.toString().toFloatOrNull() ?: 0f
                        val speed = etSpeed.text.toString().toIntOrNull() ?: 0
                        if (name.isEmpty()) {
                            PopTip.show(getString(R.string.please_input_program_name))
                            return@clickNoRepeat
                        }
                        if (program == null && viewModel.uiState.value.list.any { it.name == name }) {
                            PopTip.show(getString(R.string.program_name_exist))
                            return@clickNoRepeat
                        }

                        if (voltage !in (0f..Constants.MAX_VOLTAGE_ZM) && mode == 0) {
                            PopTip.show(getString(R.string.please_input_current_voltage))
                            return@clickNoRepeat
                        }

                        if (voltage !in (0f..Constants.MAX_VOLTAGE_RS) && mode == 1) {
                            PopTip.show(getString(R.string.please_input_current_voltage))
                            return@clickNoRepeat
                        }

                        if (time !in (0f..99f)) {
                            PopTip.show(getString(R.string.please_input_current_time))
                            return@clickNoRepeat
                        }

                        if (mode == 0 && speed !in (0..250)) {
                            PopTip.show(getString(R.string.please_input_current_speed))
                            return@clickNoRepeat
                        }

                        if (program == null) {
                            viewModel.insert(Program(name = name, model = mode, voltage = voltage, time = time, motor = speed))
                        } else {
                            viewModel.update(program.copy(name = name, model = mode, voltage = voltage, time = time, motor = speed))
                        }

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

    override fun onDestroy() {
        super.onDestroy()
        viewModel.select(null)
    }

}