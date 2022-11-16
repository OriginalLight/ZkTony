package com.zktony.www.ui.program

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.kongzue.dialogx.dialogs.MessageDialog
import com.zktony.www.R
import com.zktony.www.adapter.ProgramAdapter
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.extension.addSuffix
import com.zktony.www.common.extension.afterTextChange
import com.zktony.www.common.extension.removeZero
import com.zktony.www.common.room.entity.Program
import com.zktony.www.databinding.FragmentProgramBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProgramFragment :
    BaseFragment<ProgramViewModel, FragmentProgramBinding>(R.layout.fragment_program) {

    override val viewModel: ProgramViewModel by viewModels()

    private val programAdapter by lazy { ProgramAdapter() }
    private var program = Program()
    private var programList: List<Program> = arrayListOf()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        binding.con3.visibility = View.GONE
        binding.btn1.visibility = View.GONE
        initRadioGroup()
        buttonEvent()
        initEditText()
        initRecyclerView()
        initObserver()
    }


    /**
     * 初始化循环列表
     */
    private fun initRecyclerView() {
        binding.rc1.adapter = programAdapter
        programAdapter.setOnClick { refreshEditButton() }
    }

    /**
     * 初始化观察者
     */
    private fun initObserver() {
        lifecycleScope.launch {
            viewModel.event.distinctUntilChanged().collect {
                when (it) {
                    is ProgramEvent.VerifyProgram -> {
                        if (it.verify) {
                            binding.btn1.visibility = View.VISIBLE
                        } else {
                            binding.btn1.visibility = View.GONE
                        }
                    }

                    is ProgramEvent.ChangeProgramList -> {
                        programAdapter.submitList(it.programList)
                        programList = it.programList
                        refreshEditButton()
                    }
                }
            }
        }
    }

    /**
     * 编辑文本事件
     */
    @SuppressLint("SetTextI18n")
    private fun initEditText() {
        binding.et1.afterTextChange {
            program = program.copy(name = it)
            viewModel.verifyProgram(program)
        }
        binding.et2.afterTextChange {
            program = program.copy(proteinName = it)
            viewModel.verifyProgram(program)
        }
        binding.et3.afterTextChange {
            program = program.copy(
                proteinMinSize = if (it.isNotEmpty()) it.replace(" kD", "").removeZero()
                    .toFloat() else 0f
            )
            viewModel.verifyProgram(program)
        }
        binding.et3.addSuffix(" kD")
        binding.et4.afterTextChange {
            program = program.copy(
                proteinMaxSize = if (it.isNotEmpty()) it.replace(" kD", "")
                    .removeZero().toFloat() else 0f
            )
            viewModel.verifyProgram(program)
        }
        binding.et4.addSuffix(" kD")
        binding.et5.afterTextChange {
            program = program.copy(
                glueConcentration = if (it.isNotEmpty()) it.replace(" %", "")
                    .removeZero().toFloat() else 0f
            )
            viewModel.verifyProgram(program)
        }
        binding.et5.addSuffix(" %")
        binding.et10.afterTextChange {
            program = program.copy(
                glueMinConcentration = if (it.isNotEmpty()) it.replace(" %", "")
                    .removeZero().toFloat() else 0f
            )
            viewModel.verifyProgram(program)
        }
        binding.et10.addSuffix(" %")
        binding.et11.afterTextChange {
            program = program.copy(
                glueMaxConcentration = if (it.isNotEmpty()) it.replace(" %", "")
                    .removeZero().toFloat() else 0f
            )
            viewModel.verifyProgram(program)
        }
        binding.et11.addSuffix(" %")
        binding.et6.afterTextChange {
            program = program.copy(bufferType = it)
            viewModel.verifyProgram(program)
        }
        binding.et7.afterTextChange {
            if (it.isNotEmpty()) {
                val v = it.replace(" v", "").removeZero().toFloat()
                if (v > 65) {
                    program = program.copy(voltage = 65f)
                    binding.et7.setText("65")
                } else {
                    program = program.copy(voltage = v)
                }
            } else {
                program = program.copy(voltage = 0f)
            }
            viewModel.verifyProgram(program)
        }
        binding.et7.addSuffix(" v")
        binding.et8.afterTextChange {
            if (it.isNotEmpty()) {
                val time = it.replace(" min", "").removeZero().toFloat()
                if (time > 99) {
                    program = program.copy(time = 99f)
                    binding.et8.setText("99")
                } else {
                    program = program.copy(time = time)
                }
            } else {
                program = program.copy(time = 0f)
            }
            viewModel.verifyProgram(program)
        }
        binding.et8.addSuffix(" min")
        binding.et9.afterTextChange {
            if (it.isNotEmpty()) {
                val motor = it.replace(" rpm", "").removeZero().toInt()
                if (motor > 250) {
                    program = program.copy(motor = 250)
                    binding.et9.setText("250")
                } else {
                    program = program.copy(motor = motor)
                }
            } else {
                program = program.copy(motor = 0)
            }
            viewModel.verifyProgram(program)
        }
        binding.et9.addSuffix(" rpm")
    }

    /**
     * 按钮事件
     */
    @SuppressLint("SetTextI18n")
    private fun buttonEvent() {
        // 确定
        binding.btn1.setOnClickListener {
            if (isRepeatName(program)) {
                MessageDialog.build()
                    .setTitle("模块异常")
                    .setMessage("已存在相同名称")
                    .setOkButton("确定") { dialog, _ ->
                        dialog.dismiss()
                        true
                    }
                    .show()
                return@setOnClickListener
            } else {
                if (isInsertProgram(program)) {
                    viewModel.insertProgram(program)
                } else {
                    program = program.copy(upload = 0)
                    viewModel.updateProgram(program)
                }
            }
            binding.con3.visibility = View.GONE
            binding.con2.visibility = View.VISIBLE
        }
        // 取消
        binding.btn2.setOnClickListener {
            binding.con3.visibility = View.GONE
            binding.con2.visibility = View.VISIBLE
            program = Program()
            initAddEditForm(program)
        }
        // 新建
        binding.btn3.setOnClickListener {
            binding.tv1.text = "添加程序"
            program = Program()
            initAddEditForm(program)
            binding.con3.visibility = View.VISIBLE
            binding.con2.visibility = View.GONE
        }
        // 删除
        binding.btn4.setOnClickListener {
            val selectedProgram = programAdapter.getItem()
            MessageDialog.build()
                .setTitle("删除实验程序")
                .setMessage("确定删除实验程序：" + selectedProgram.name + "？")
                .setOkButton("确定") { dialog, _ ->
                    viewModel.deleteProgram(selectedProgram)
                    programAdapter.isClick = true
                    programAdapter.currentPosition = 0
                    refreshEditButton()
                    dialog.dismiss()
                    true
                }
                .setCancelButton("取消") { dialog, _ ->
                    dialog.dismiss()
                    true
                }
                .show()
        }
        // 编辑
        binding.btn5.setOnClickListener {
            binding.tv1.text = "编辑程序"
            binding.con3.visibility = View.VISIBLE
            binding.con2.visibility = View.GONE
            program = programAdapter.getItem()
            initAddEditForm(program)
        }
    }

    /**
     * 转膜还是染色的选项
     */
    @SuppressLint("NonConstantResourceId")
    private fun initRadioGroup() {
        binding.et6.visibility = View.GONE
        binding.rg1.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_1 -> program = program.copy(thickness = "0.075")
                R.id.rb_2 -> program = program.copy(thickness = "0.1")
                R.id.rb_3 -> program = program.copy(thickness = "0.15")
            }
            viewModel.verifyProgram(program)
        }
        binding.rg2.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_4 -> {
                    program = program.copy(bufferType = "厂家")
                    binding.et6.visibility = View.GONE
                }

                R.id.rb_5 -> {
                    if (program.bufferType == "厂家") {
                        program = program.copy(bufferType = "")
                    }
                    binding.et6.visibility = View.VISIBLE
                }
            }
            viewModel.verifyProgram(program)
        }
        binding.rg3.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_6 -> {
                    binding.l10.visibility = View.VISIBLE
                    program = program.copy(model = 0)
                }

                R.id.rb_7 -> {
                    binding.l10.visibility = View.GONE
                    program = program.copy(model = 1, motor = 0)
                }
            }
            viewModel.verifyProgram(program)
        }
        binding.rg4.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_8 -> {
                    binding.conT.visibility = View.GONE
                    binding.et5.visibility = View.VISIBLE
                    binding.et10.setText("")
                    binding.et11.setText("")
                    program = program.copy(
                        glueMinConcentration = 0f,
                        glueMaxConcentration = 0f,
                        glueType = 0
                    )
                }

                R.id.rb_9 -> {
                    binding.conT.visibility = View.VISIBLE
                    binding.et5.visibility = View.GONE
                    binding.et5.setText("")
                    program = program.copy(glueConcentration = 0f, glueType = 1)
                }
            }
            viewModel.verifyProgram(program)
        }
    }

    /**
     * 初始化添加/编辑界面
     */
    @SuppressLint("SetTextI18n")
    private fun initAddEditForm(program: Program) {
        if (program.name.isNotEmpty()) {
            binding.et1.setText(program.name)
        } else {
            binding.et1.setText("")
        }
        if (program.proteinName.isNotEmpty()) {
            binding.et2.setText(program.proteinName)
        } else {
            binding.et2.setText("")
        }
        if (program.proteinMinSize != 0f) {
            binding.et3.setText(program.proteinMinSize.toString().removeZero() + " kD")
        } else {
            binding.et3.setText("")
        }
        if (program.proteinMaxSize != 0f) {
            binding.et4.setText(program.proteinMaxSize.toString().removeZero() + " kD")
        } else {
            binding.et4.setText("")
        }
        if (program.glueType == 0) {
            binding.rb8.isChecked = true
            if (program.glueConcentration != 0f) {
                binding.et5.setText(program.glueConcentration.toString().removeZero() + " %")
            } else {
                binding.et5.setText("")
            }
            binding.conT.visibility = View.GONE
            binding.et10.setText("")
            binding.et11.setText("")
        } else {
            binding.rb9.isChecked = true
            binding.et5.setText("")
            binding.et5.visibility = View.GONE
            binding.conT.visibility = View.VISIBLE
            if (program.glueMinConcentration != 0f) {
                binding.et10.setText(program.glueMinConcentration.toString().removeZero() + " %")
            } else {
                binding.et10.setText("")
            }
            if (program.glueMaxConcentration != 0f) {
                binding.et11.setText(program.glueMaxConcentration.toString().removeZero() + " %")
            } else {
                binding.et11.setText("")
            }
        }
        when (program.thickness) {
            "0.075" -> binding.rb1.isChecked = true
            "0.1" -> binding.rb2.isChecked = true
            "0.15" -> binding.rb3.isChecked = true
        }
        if (program.bufferType == "厂家") {
            binding.rb4.isChecked = true
            binding.et6.visibility = View.GONE
        } else {
            binding.rb5.isChecked = true
            binding.et6.visibility = View.VISIBLE
            binding.et6.setText(program.bufferType)
        }
        if (program.model == 0) {
            binding.rb6.isChecked = true
            binding.l10.visibility = View.VISIBLE
        } else {
            binding.rb7.isChecked = true
            binding.l10.visibility = View.GONE
        }
        if (program.voltage != 0f) {
            binding.et7.setText(java.lang.String.valueOf(program.voltage).removeZero() + " v")
        } else {
            binding.et7.setText("")
        }
        if (program.time != 0f) {
            binding.et8.setText(java.lang.String.valueOf(program.time).removeZero() + " min")
        } else {
            binding.et8.setText("")
        }
        if (program.motor != 0) {
            binding.et9.setText(program.motor.toString() + " rpm")
        } else {
            binding.et9.setText("")
        }
    }


    /**
     * 判断是否是插入
     * @param program [Program] 程序
     */
    private fun isInsertProgram(program: Program): Boolean {
        programList.filter { it.id == program.id }.forEach { _ ->
            return false
        }
        return true
    }

    /**
     * 名字是否重复
     * @param program [Program] 程序
     */
    private fun isRepeatName(program: Program): Boolean {
        programList.filter { it.name == program.name && it.id != program.id }.forEach { _ ->
            return true
        }
        return false
    }

    /**
     * 刷新按钮
     */
    private fun refreshEditButton() {
        if (programList.isEmpty() || programAdapter.currentPosition == -1 || !programAdapter.isClick) {
            binding.btn4.visibility = View.GONE
            binding.btn5.visibility = View.GONE
        } else {
            binding.btn4.visibility = View.VISIBLE
            binding.btn5.visibility = View.VISIBLE
        }
    }

}