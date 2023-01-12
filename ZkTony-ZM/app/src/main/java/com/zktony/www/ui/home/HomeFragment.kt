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
import com.zktony.www.common.extension.*
import com.zktony.www.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

@AndroidEntryPoint
class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>(R.layout.fragment_home) {

    override val viewModel: HomeViewModel by viewModels()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initTabLayout(0)
        initTabLayout(1)
        initButton(0)
        initButton(1)
        initEditText(0)
        initEditText(1)
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

    /**
     * 初始化TabLayout
     */
    private fun initTabLayout(xy: Int) {
        val bind = if (xy == 0) binding.x else binding.y
        bind.tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (viewModel.uiStateX.value.job != null) return
                viewModel.setModel(tab?.position ?: 0, xy)
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
    private fun initButton(xy: Int) {
        val bind = if (xy == 0) binding.x else binding.y
        bind.run {
            selector.run {
                clickScale()
                setOnClickListener {
                    val uiState = if (xy == 0) viewModel.uiStateX else viewModel.uiStateY
                    if (uiState.value.job != null) return@setOnClickListener
                    val programList = viewModel.programList.value
                    val model = uiState.value.model
                    val menuList = programList.filter { it.model == model }.map { it.name }
                    if (menuList.size < 2) {
                        PopTip.show("没有更多程序！")
                    } else {
                        PopMenu.show(bind.selector, menuList)
                            .setOverlayBaseView(false)
                            .setMenuTextInfo(TextInfo().apply {
                                gravity = Gravity.CENTER
                                fontSize = 16
                            }).setOnMenuItemClickListener { _, text, _ ->
                                viewModel.selectProgram(
                                    programList.find { it.name == text.toString() },
                                    xy
                                )
                                false
                            }
                            .setRadius(0f)
                            .alignGravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    }
                }
            }
            stop.setOnClickListener { viewModel.stop(xy) }
            start.setOnClickListener { viewModel.start(xy) }
            pumpUp.addTouchEvent({
                it.scaleX = 0.9f
                it.scaleY = 0.9f
                viewModel.pumpUpOrBack(0, 0, xy)
            }, {
                it.scaleX = 1f
                it.scaleY = 1f
                viewModel.pumpUpOrBack(0, 1, xy)
            })
            pumpBack.addTouchEvent({
                it.scaleX = 0.9f
                it.scaleY = 0.9f
                viewModel.pumpUpOrBack(1, 0, xy)
            }, {
                it.scaleX = 1f
                it.scaleY = 1f
                viewModel.pumpUpOrBack(1, 1, xy)
            })
        }
    }

    /**
     * 初始化EditText
     */
    @SuppressLint("SetTextI18n")
    private fun initEditText(xy: Int) {
        lifecycleScope.launch {
            delay(100L)
            val bind = if (xy == 0) binding.x else binding.y
            bind.run {
                motor.afterTextChange {
                    val motor = it.replace(" RPM", "").removeZero().toIntOrNull() ?: 0
                    viewModel.setMotor(min(250, motor), xy)
                    if (motor > 250) {
                        bind.motor.setText("250")
                    }
                }
                motor.addSuffix(" RPM")
                voltage.afterTextChange {
                    val voltage = it.replace(" V", "").removeZero().toFloatOrNull() ?: 0f
                    viewModel.setVoltage(minOf(65f, voltage), xy)
                    if (voltage > 65f) {
                        bind.voltage.setText("65")
                    }
                }
                voltage.addSuffix(" V")
                time.afterTextChange {
                    val time = it.replace(" MIN", "").removeZero().toFloatOrNull() ?: 0f
                    viewModel.setTime(minOf(99f, time), xy)
                    if (time > 99f) {
                        bind.time.setText("99")
                    }
                }
                time.addSuffix(" MIN")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUiState(xy: Int, uiState: HomeUiState) {
        val bind = if (xy == 0) binding.x else binding.y
        // 功能选择部分
        if (uiState.model == 0) {
            bind.run {
                tabLayout.getTabAt(0)?.select()
                pump.visibility = View.VISIBLE
                motor.isEnabled = true
            }
        } else {
            bind.run {
                tabLayout.getTabAt(1)?.select()
                pump.visibility = View.GONE
                motor.isEnabled = false
            }

        }
        // 程序选择部分
        bind.run {
            programName.text = uiState.programName
            if (uiState.model == 0) {
                if (motor.isFocused.not()) {
                    if (uiState.motor > 0) {
                        motor.setText(uiState.motor.toString() + " RPM")
                    } else {
                        motor.setText("")
                    }
                }
            } else {
                motor.setText("/")
            }

            if (uiState.programName == "洗涤") {
                voltage.isEnabled = false
                voltage.setText("/")
            } else {
                voltage.isEnabled = true
                if (voltage.isFocused.not()) {
                    if (uiState.voltage > 0f) {
                        voltage.setText(uiState.voltage.toString().removeZero() + " V")
                    } else {
                        voltage.setText("")
                    }
                }
            }
            if (time.isFocused.not()) {
                if (uiState.time > 0) {
                    time.setText(uiState.time.toString().removeZero() + " MIN")
                } else {
                    time.setText("")
                }
            }
        }
        // 实时信息显示部分
        if (uiState.currentStatus == 0) {
            bind.run {
                currentStatus.text = "模块${if (xy == 0) "A" else "B"}未插入"
                currentStatus.setBackgroundColor(Color.parseColor("#41D50000"))
            }
            if (uiState.currentTime == "已完成") {
                viewModel.setCurrentTime(xy)
            }
        } else {
            bind.run {
                currentStatus.text = "模块${if (xy == 0) "A" else "B"}已就绪"
                currentStatus.setBackgroundColor(Color.parseColor("#287DF133"))
            }
        }

        if (uiState.currentMotor == 0) {
            bind.currentMotor.text = "OFF"
        } else {
            bind.currentMotor.text = uiState.currentMotor.toString() + " RPM"
        }

        if (uiState.currentVoltage == 0f) {
            bind.run {
                currentVoltage.text = "OFF"
                currentVoltage.setBackgroundColor(Color.parseColor("#287DF133"))
            }

        } else {
            val sub = if (uiState.voltage > 10f) 5 else 4
            bind.run {
                currentVoltage.text =
                    uiState.currentVoltage.toString().removeZero()
                        .substring(0, sub) + " V"
                currentVoltage.setBackgroundColor(Color.parseColor("#287DF133"))
            }
            if (uiState.currentVoltage > uiState.voltage + 1 || uiState.currentVoltage < uiState.voltage - 1) {
                bind.currentVoltage.setBackgroundColor(Color.parseColor("#36FFD600"))
            }
        }
        if (uiState.currentCurrent == 0f) {
            bind.run {
                currentCurrent.text = "OFF"
                currentCurrent.setBackgroundColor(Color.parseColor("#287DF133"))
            }
        } else {
            if (uiState.currentCurrent < 1f && uiState.currentCurrent > 0f) {
                bind.run {
                    currentCurrent.text =
                        (uiState.currentCurrent * 1000).toInt().toString() + " mA"
                    currentCurrent.setBackgroundColor(Color.parseColor("#287DF133"))
                }
                if (uiState.currentCurrent < 0.05f) {
                    bind.currentCurrent.setBackgroundColor(Color.parseColor("#41D50000"))
                }
            } else {
                bind.run {
                    currentCurrent.text = uiState.currentCurrent.toString().removeZero()
                        .substring(0, 4) + " A"
                    currentCurrent.setBackgroundColor(Color.parseColor("#287DF133"))
                }
            }
        }
        // button的状态
        if (uiState.job != null) {
            bind.start.isEnabled = false
        } else {
            if (uiState.model == 0) {
                if (uiState.programName == "洗涤") {
                    bind.start.isEnabled =
                        uiState.motor > 0 && uiState.time > 0f && uiState.currentStatus == 1
                } else {
                    bind.start.isEnabled =
                        uiState.motor > 0 && uiState.time > 0f && uiState.voltage > 0f && uiState.currentStatus == 1
                }
            } else {
                bind.start.isEnabled =
                    uiState.time > 0f && uiState.voltage > 0f && uiState.currentStatus == 1
            }
        }
        // time
        bind.currentTime.text = uiState.currentTime
        // 运行中锁定界面
        if (uiState.job != null) {
            bind.run {
                tabLayout.disable(false)
                motor.isEnabled = false
                voltage.isEnabled = false
                time.isEnabled = false
            }
        } else {
            bind.run {
                tabLayout.disable(true)
                motor.isEnabled = uiState.model == 0
                voltage.isEnabled = uiState.programName != "洗涤"
                time.isEnabled = true
            }
        }
    }

}