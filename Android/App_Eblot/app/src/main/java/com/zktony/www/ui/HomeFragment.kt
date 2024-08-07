package com.zktony.www.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.lifecycle.*
import com.google.android.material.tabs.TabLayout
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.*
import com.zktony.core.utils.Constants.MAX_MOTOR
import com.zktony.core.utils.Constants.MAX_TIME
import com.zktony.www.R
import com.zktony.www.core.ext.spannerDialog
import com.zktony.www.databinding.FragmentHomeBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>(R.layout.fragment_home) {

    override val viewModel: HomeViewModel by viewModel()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()
    }

    /**
     * 初始化Flow收集器
     */
    @SuppressLint("SetTextI18n")
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiStateX.collect {
                        setUiState(0, it)
                    }

                }
                launch {
                    viewModel.uiStateY.collect {
                        setUiState(1, it)
                    }
                }

            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initView() {
        lifecycleScope.launch {
            for (i in 0..1) {
                val bind = if (i == 0) binding.x else binding.y
                bind.apply {
                    stop.clickNoRepeat { viewModel.stop(i) }
                    start.clickNoRepeat { viewModel.start(i) }

                    pumpUp.clickNoRepeat { viewModel.pumpUpOrBack(0, i) }
                    pumpBack.clickNoRepeat { viewModel.pumpUpOrBack(1, i) }

                    bind.tabLayout.addOnTabSelectedListener(object :
                        TabLayout.OnTabSelectedListener {
                        override fun onTabSelected(tab: TabLayout.Tab?) {
                            viewModel.setModel(tab?.position ?: 0, i)
                        }

                        override fun onTabUnselected(tab: TabLayout.Tab?) {
                        }

                        override fun onTabReselected(tab: TabLayout.Tab?) {
                        }
                    })

                    // 设置tabitem的文字
                    val tabLayout = bind.tabLayout
                    val tabItem1 = tabLayout.getTabAt(0)
                    val tabItem2 = tabLayout.getTabAt(1)
                    tabItem1?.text = "${getString(R.string.transfer)} ${'A' + i}"
                    tabItem2?.text = "${getString(R.string.stain)} ${'A' + i}"


                    with(selector) {
                        clickScale()
                        clickNoRepeat {
                            val uiState = if (i == 0) viewModel.uiStateX else viewModel.uiStateY
                            if (uiState.value.job != null) return@clickNoRepeat
                            val programList = viewModel.programList.value
                            val model = uiState.value.model
                            val menuList = programList.filter { it.model == model }.map { it.name }
                            if (menuList.size >= 2) {
                                spannerDialog(view = this, menu = menuList) { text, _ ->
                                    viewModel.selectProgram(programList.find { it.name == text }, i)
                                }
                            }
                        }
                    }
                    delay(100L)
                    with(motor) {
                        afterTextChange {
                            viewModel.setMotor(
                                motor = it.toIntOrNull() ?: 0,
                                xy = i,
                                block = { bind.motor.setText(MAX_MOTOR.toString()) }
                            )
                        }
                    }
                    with(voltage) {
                        afterTextChange {
                            viewModel.setVoltage(
                                voltage = it.toFloatOrNull() ?: 0f,
                                xy = i,
                                block = { max -> bind.voltage.setText(max.format()) }
                            )
                        }
                    }
                    with(time) {
                        afterTextChange {
                            viewModel.setTime(
                                time = it.toFloatOrNull() ?: 0f,
                                xy = i,
                                block = { bind.time.setText(MAX_TIME.format()) }
                            )
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUiState(xy: Int, uiState: HomeUiState) {
        val bind = if (xy == 0) binding.x else binding.y
        // 功能选择部分
        if (uiState.model == 0) {
            bind.apply {
                tabLayout.getTabAt(0)?.select()
                pump.visibility = View.VISIBLE
                layoutSpeed.visibility = View.VISIBLE
            }
        } else {
            bind.apply {
                tabLayout.getTabAt(1)?.select()
                pump.visibility = View.GONE
                layoutSpeed.visibility = View.GONE
            }
        }
        // 程序选择部分
        bind.apply {
            selector.text = uiState.programName
            if (uiState.model == 0) {
                if (motor.isFocused.not()) {
                    if (uiState.motor > 0) {
                        motor.setText(uiState.motor.toString())
                    } else {
                        motor.setText("")
                    }
                }
            }

            if (uiState.model == 0) {
                voltage.hint = "0 - 55"
            } else {
                voltage.hint = "0 - 30"
            }

            if (uiState.programName == getString(R.string.wash)) {
                voltage.isEnabled = false
                voltage.setText("/")
            } else {
                voltage.isEnabled = true
                if (voltage.isFocused.not()) {
                    if (uiState.voltage > 0f) {
                        voltage.setText(uiState.voltage.format())
                    } else {
                        voltage.setText("")
                    }
                }
            }
            if (time.isFocused.not()) {
                if (uiState.time > 0) {
                    time.setText(uiState.time.format())
                } else {
                    time.setText("")
                }
            }
        }
        // 实时信息显示部分
        bind.apply {
            currentStatus.text = if (xy == 0) "${getString(R.string.channel)} A" else "${getString(R.string.channel)} B"
        }

        if (uiState.currentMotor == 0) {
            bind.currentMotor.text = "OFF"
        } else {
            bind.currentMotor.text = uiState.currentMotor.toString() + " RPM"
        }

        if (uiState.currentVoltage == 0f) {
            bind.apply {
                currentVoltage.text = "OFF"
            }

        } else {
            val sub = if (uiState.voltage > 10f) 5 else 4
            bind.apply {
                currentVoltage.text =
                    uiState.currentVoltage.format()
                        .substring(0, sub) + " V"
            }
        }
        if (uiState.currentCurrent == 0f) {
            bind.apply {
                currentCurrent.text = "OFF"
            }
        } else {
            if (uiState.currentCurrent < 1f && uiState.currentCurrent > 0f) {
                bind.apply {
                    currentCurrent.text =
                        (uiState.currentCurrent * 1000).toInt().toString() + " mA"
                }

            } else {
                bind.apply {
                    currentCurrent.text = uiState.currentCurrent.format()
                        .substring(0, 4) + " A"
                }
            }
        }
        // button的状态
        if (uiState.job != null) {
            bind.start.isEnabled = false
            bind.stop.isEnabled = true
            bind.pumpUp.isEnabled = false
            bind.pumpBack.isEnabled = false
        } else {
            if (uiState.model == 0) {
                if (uiState.programName == getString(R.string.wash)) {
                    bind.start.isEnabled =
                        uiState.motor > 0 && uiState.time > 0f
                } else {
                    bind.start.isEnabled =
                        uiState.motor > 0 && uiState.time > 0f && uiState.voltage > 0f
                }
            } else {
                bind.start.isEnabled =
                    uiState.time > 0f && uiState.voltage > 0f

            }
            bind.stop.isEnabled = false
            bind.pumpUp.isEnabled = true
            bind.pumpBack.isEnabled = true
        }
        // time
        bind.currentTime.text = uiState.currentTime
        // 运行中锁定界面
        if (uiState.job != null) {
            bind.apply {
                tabLayout.disable(false)
                motor.isEnabled = false
                voltage.isEnabled = false
                time.isEnabled = false
            }
        } else {
            bind.apply {
                tabLayout.disable(true)
                motor.isEnabled = uiState.model == 0
                voltage.isEnabled = uiState.programName != getString(R.string.wash)
                time.isEnabled = true
            }
        }
    }

}