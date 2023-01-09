package com.zktony.www.ui.home

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.tabs.TabLayout
import com.kongzue.dialogx.dialogs.PopMenu
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.util.TextInfo
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.extension.*
import com.zktony.www.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.min

@AndroidEntryPoint
class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>(R.layout.fragment_home) {

    override val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var appViewModel: AppViewModel

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initTabLayout()
        initButton()
        initEditText()
    }

    /**
     * 初始化Flow收集器
     */
    @SuppressLint("SetTextI18n")
    private fun initFlowCollector() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiStateX.collect {
                        // 功能选择部分
                        if (it.model == 0) {
                            binding.x.run {
                                tabLayout.getTabAt(0)?.select()
                                pump.visibility = View.VISIBLE
                                motor.isEnabled = true
                            }
                        } else {
                            binding.x.run {
                                tabLayout.getTabAt(1)?.select()
                                pump.visibility = View.GONE
                                motor.isEnabled = false
                            }
                        }
                        // 程序选择部分
                        binding.x.run {
                            programName.text = it.programName
                            if (it.model == 0) {
                                if (motor.isFocused.not()) {
                                    if (it.motor > 0) {
                                        motor.setText(it.motor.toString() + " RPM")
                                    } else {
                                        motor.setText("")
                                    }
                                }
                            } else {
                                motor.setText("/")
                            }

                            if (it.programName == "洗涤") {
                                voltage.isEnabled = false
                                voltage.setText("/")
                            } else {
                                voltage.isEnabled = true
                                if (voltage.isFocused.not()) {
                                    if (it.voltage > 0f) {
                                        voltage.setText(it.voltage.toString().removeZero() + " V")
                                    } else {
                                        voltage.setText("")
                                    }
                                }
                            }
                            if (time.isFocused.not()) {
                                if (it.time > 0) {
                                    time.setText(it.time.toString().removeZero() + " MIN")
                                } else {
                                    time.setText("")
                                }
                            }
                        }
                        // 实时信息显示部分
                        if (it.currentStatus == 0) {
                            binding.x.run {
                                currentStatus.text = "模块A未插入"
                                currentStatus.setBackgroundColor(Color.parseColor("#41D50000"))
                            }
                        } else {
                            binding.x.run {
                                currentStatus.text = "模块A已就绪"
                                currentStatus.setBackgroundColor(Color.parseColor("#287DF133"))
                            }
                        }
                        if (it.currentMotor == 0) {
                            binding.x.currentMotor.text = "OFF"
                        } else {
                            binding.x.currentMotor.text = it.currentMotor.toString() + " RPM"
                        }
                        if (it.currentVoltage == 0f) {
                            binding.x.currentVoltage.text = "OFF"
                        } else {
                            val sub = if (it.voltage > 10f) 5 else 4
                            binding.x.currentVoltage.text =
                                it.currentVoltage.toString().removeZero().substring(0, sub) + " V"
                            if (it.currentVoltage > it.voltage + 1 || it.currentVoltage < it.voltage - 1) {
                                binding.x.voltage.setBackgroundColor(Color.parseColor("#36FFD600"))
                            }
                        }
                        if (it.currentCurrent == 0f) {
                            binding.x.currentCurrent.text = "OFF"
                        } else {
                            if (it.currentCurrent < 1f) {
                                binding.x.currentCurrent.text =
                                    (it.currentCurrent * 1000).toInt().toString() + " mA"
                                if (it.currentCurrent < 0.1f) {
                                    binding.x.currentCurrent.setBackgroundColor(Color.parseColor("#41D50000"))
                                }
                            } else {
                                binding.x.currentCurrent.text =
                                    it.currentCurrent.toString().removeZero().substring(0, 4) + " A"
                            }
                            binding.x.currentCurrent.text =
                                it.currentCurrent.toString().removeZero()
                        }
                        // button的状态
                        if (it.job != null) {
                            binding.x.start.isEnabled = false
                        } else {
                            if (it.model == 0) {
                                if (it.programName == "洗涤") {
                                    binding.x.start.isEnabled = it.motor > 0 && it.time > 0f
                                } else {
                                    binding.x.start.isEnabled =
                                        it.motor > 0 && it.time > 0f && it.voltage > 0f
                                }
                            } else {
                                binding.x.start.isEnabled = it.time > 0f && it.voltage > 0f
                            }
                        }
                        // time
                        binding.x.currentTime.text = it.currentTime.getTimeFormat()
                        // 运行中锁定界面
                        if (it.job != null) {
                            binding.x.run {
                                tabLayout.disable(false)
                                motor.isEnabled = false
                                voltage.isEnabled = false
                                time.isEnabled = false
                            }
                        } else {
                            binding.x.run {
                                tabLayout.disable(true)
                                motor.isEnabled = it.model == 0
                                voltage.isEnabled = it.programName != "洗涤"
                                time.isEnabled = true
                            }
                        }
                    }
                }
                launch {
                    viewModel.uiStateY.collect {
                        // 功能选择部分
                        if (it.model == 0) {
                            binding.y.run {
                                tabLayout.getTabAt(0)?.select()
                                pump.visibility = View.VISIBLE
                                motor.isEnabled = true
                            }
                        } else {
                            binding.y.run {
                                tabLayout.getTabAt(1)?.select()
                                pump.visibility = View.GONE
                                motor.isEnabled = false
                            }
                        }
                        // 程序选择部分
                        binding.y.run {
                            programName.text = it.programName
                            if (it.model == 0) {
                                if (motor.isFocused.not()) {
                                    if (it.motor > 0) {
                                        motor.setText(it.motor.toString() + " RPM")
                                    } else {
                                        motor.setText("")
                                    }
                                }
                            } else {
                                motor.setText("/")
                            }

                            if (it.programName == "洗涤") {
                                voltage.isEnabled = false
                                voltage.setText("/")
                            } else {
                                voltage.isEnabled = true
                                if (voltage.isFocused.not()) {
                                    if (it.voltage > 0f) {
                                        voltage.setText(it.voltage.toString().removeZero() + " V")
                                    } else {
                                        voltage.setText("")
                                    }
                                }
                            }
                            if (time.isFocused.not()) {
                                if (it.time > 0) {
                                    time.setText(it.time.toString().removeZero() + " MIN")
                                } else {
                                    time.setText("")
                                }
                            }
                        }
                        // 实时信息显示部分
                        if (it.currentStatus == 0) {
                            binding.y.run {
                                currentStatus.text = "模块B未插入"
                                currentStatus.setBackgroundColor(Color.parseColor("#41D50000"))
                            }
                        } else {
                            binding.y.run {
                                currentStatus.text = "模块B已就绪"
                                currentStatus.setBackgroundColor(Color.parseColor("#287DF133"))
                            }
                        }
                        if (it.currentMotor == 0) {
                            binding.y.currentMotor.text = "OFF"
                        } else {
                            binding.y.currentMotor.text = it.currentMotor.toString() + " RPM"
                        }
                        if (it.currentVoltage == 0f) {
                            binding.y.currentVoltage.text = "OFF"
                        } else {
                            val sub = if (it.voltage > 10f) 5 else 4
                            binding.y.currentVoltage.text =
                                it.currentVoltage.toString().removeZero().substring(0, sub) + " V"
                            if (it.currentVoltage > it.voltage + 1 || it.currentVoltage < it.voltage - 1) {
                                binding.y.voltage.setBackgroundColor(Color.parseColor("#36FFD600"))
                            }
                        }
                        if (it.currentCurrent == 0f) {
                            binding.y.currentCurrent.text = "OFF"
                        } else {
                            if (it.currentCurrent < 1f) {
                                binding.y.currentCurrent.text =
                                    (it.currentCurrent * 1000).toInt().toString() + " mA"
                                if (it.currentCurrent < 0.1f) {
                                    binding.y.currentCurrent.setBackgroundColor(Color.parseColor("#41D50000"))
                                }
                            } else {
                                binding.y.currentCurrent.text =
                                    it.currentCurrent.toString().removeZero().substring(0, 4) + " A"
                            }
                            binding.y.currentCurrent.text =
                                it.currentCurrent.toString().removeZero()
                        }
                        // button的状态
                        if (it.job != null) {
                            binding.y.start.isEnabled = false
                        } else {
                            if (it.model == 0) {
                                if (it.programName == "洗涤") {
                                    binding.y.start.isEnabled = it.motor > 0 && it.time > 0f
                                } else {
                                    binding.y.start.isEnabled =
                                        it.motor > 0 && it.time > 0f && it.voltage > 0f
                                }
                            } else {
                                binding.y.start.isEnabled = it.time > 0f && it.voltage > 0f
                            }
                        }
                        // time
                        binding.y.currentTime.text = it.currentTime.getTimeFormat()
                        // 运行中锁定界面
                        if (it.job != null) {
                            binding.y.run {
                                tabLayout.disable(false)
                                motor.isEnabled = false
                                voltage.isEnabled = false
                                time.isEnabled = false
                            }
                        } else {
                            binding.y.run {
                                tabLayout.disable(true)
                                motor.isEnabled = it.model == 0
                                voltage.isEnabled = it.programName != "洗涤"
                                time.isEnabled = true
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 初始化TabLayout
     */
    private fun initTabLayout() {
        binding.x.tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (viewModel.uiStateX.value.job != null) return
                viewModel.setModel(tab?.position ?: 0, 0)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
        binding.y.tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (viewModel.uiStateY.value.job != null) return
                viewModel.setModel(tab?.position ?: 0, 1)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    /**
     * 初始化按钮
     */
    private fun initButton() {
        binding.x.run {
            selector.run {
                clickScale()
                setOnClickListener {
                    if (viewModel.uiStateX.value.job != null) return@setOnClickListener
                    val programList = viewModel.programList.value
                    val model = viewModel.uiStateX.value.model
                    val menuList = programList.filter { it.model == model }.map { it.name }
                    if (menuList.size < 2) {
                        PopTip.show("没有更多程序！")
                    } else {
                        PopMenu.show(menuList).setMenuTextInfo(TextInfo().apply {
                            gravity = Gravity.CENTER
                            fontSize = 16
                        }).setOnMenuItemClickListener { _, text, _ ->
                            viewModel.selectProgram(
                                programList.find { it.name == text.toString() },
                                0
                            )
                            false
                        }.width = 300
                    }
                }
            }
            stop.setOnClickListener { }
            start.setOnClickListener { viewModel.start(0) }
            pumpUp.addTouchEvent({
                it.scaleX = 0.9f
                it.scaleY = 0.9f
                viewModel.pumpUpOrBack(0, 0, 0)
            }, {
                it.scaleX = 1f
                it.scaleY = 1f
                viewModel.pumpUpOrBack(0, 1, 0)
            })
            pumpBack.addTouchEvent({
                it.scaleX = 0.9f
                it.scaleY = 0.9f
                viewModel.pumpUpOrBack(1, 0, 0)
            }, {
                it.scaleX = 1f
                it.scaleY = 1f
                viewModel.pumpUpOrBack(1, 1, 0)
            })
        }
        binding.y.run {
            selector.run {
                setOnClickListener {
                    if (viewModel.uiStateY.value.job != null) return@setOnClickListener
                    val programList = viewModel.programList.value
                    val model = viewModel.uiStateY.value.model
                    val menuList = programList.filter { it.model == model }.map { it.name }
                    if (menuList.size < 2) {
                        PopTip.show("没有更多程序！")
                    } else {
                        PopMenu.show(menuList).setMenuTextInfo(TextInfo().apply {
                            gravity = Gravity.CENTER
                            fontSize = 16
                        }).setOnMenuItemClickListener { _, text, _ ->
                            viewModel.selectProgram(
                                programList.find { it.name == text.toString() },
                                1
                            )
                            false
                        }.width = 300
                    }
                }
            }
            stop.setOnClickListener { }
            start.setOnClickListener { viewModel.start(1) }
            pumpUp.addTouchEvent({
                it.scaleX = 0.9f
                it.scaleY = 0.9f
                viewModel.pumpUpOrBack(0, 0, 1)
            }, {
                it.scaleX = 1f
                it.scaleY = 1f
                viewModel.pumpUpOrBack(0, 1, 1)
            })
            pumpBack.addTouchEvent({
                it.scaleX = 0.9f
                it.scaleY = 0.9f
                viewModel.pumpUpOrBack(1, 0, 1)
            }, {
                it.scaleX = 1f
                it.scaleY = 1f
                viewModel.pumpUpOrBack(1, 1, 1)
            })
        }
    }

    /**
     * 初始化EditText
     */
    @SuppressLint("SetTextI18n")
    private fun initEditText() {
        binding.x.run {
            motor.afterTextChange {
                val motor = it.replace(" RPM", "").removeZero().toIntOrNull() ?: 0
                viewModel.setMotor(min(250, motor), 0)
                if (motor > 250) {
                    binding.x.motor.setText("250")
                }
            }
            motor.addSuffix(" RPM")
            voltage.afterTextChange {
                val voltage = it.replace(" V", "").removeZero().toFloatOrNull() ?: 0f
                viewModel.setVoltage(minOf(65f, voltage), 0)
                if (voltage > 65f) {
                    binding.x.voltage.setText("65")
                }
            }
            voltage.addSuffix(" V")
            time.afterTextChange {
                val time = it.replace(" MIN", "").removeZero().toFloatOrNull() ?: 0f
                viewModel.setTime(minOf(99f, time), 0)
                if (time > 99f) {
                    binding.x.time.setText("99")
                }
            }
            time.addSuffix(" MIN")
        }
        binding.y.run {
            motor.afterTextChange {
                val motor = it.replace(" RPM", "").removeZero().toIntOrNull() ?: 0
                viewModel.setMotor(min(250, motor), 1)
                if (motor > 250) {
                    binding.y.motor.setText("250")
                }
            }
            motor.addSuffix(" RPM")
            voltage.afterTextChange {
                val voltage = it.replace(" V", "").removeZero().toFloatOrNull() ?: 0f
                viewModel.setVoltage(minOf(65f, voltage), 1)
                if (voltage > 65f) {
                    binding.y.voltage.setText("65")
                }
            }
            voltage.addSuffix(" V")
            time.afterTextChange {
                val time = it.replace(" MIN", "").removeZero().toFloatOrNull() ?: 0f
                viewModel.setTime(minOf(99f, time), 1)
                if (time > 99f) {
                    binding.y.time.setText("99")
                }
            }
            time.addSuffix(" MIN")
        }
    }

}