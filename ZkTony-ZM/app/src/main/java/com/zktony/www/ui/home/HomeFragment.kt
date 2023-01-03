package com.zktony.www.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.kongzue.dialogx.dialogs.MessageDialog
import com.zktony.www.R
import com.zktony.www.adapter.SpinnerAdapter
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.extension.*
import com.zktony.www.data.model.Program
import com.zktony.www.common.utils.Logger
import com.zktony.www.databinding.FragmentHomeBinding
import com.zktony.www.serial.protocol.V1
import com.zktony.www.ui.home.Model.*
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>(R.layout.fragment_home) {

    override val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var appViewModel: AppViewModel

    lateinit var adapterX: SpinnerAdapter<Program>
    lateinit var adapterY: SpinnerAdapter<Program>
    private val programListA = arrayListOf<Program>()
    private val programListB = arrayListOf<Program>()
    private val programListX = arrayListOf<Program>()
    private val programListY = arrayListOf<Program>()

    private var state = ControlState()
    private var xDisposable: Disposable? = null
    private var yDisposable: Disposable? = null
    private var zDisposable: Disposable? = null

    override fun onViewCreated(savedInstanceState: Bundle?) {
        // 初始化监视器
        initObserver()
        // 初始化Spinner
        initSp()
        // 根据Tab选择模式
        tab1Event()
        // 给输入参数加单位
        et1Et2Et3Event()
        // 当填充和回吸按下的时候
        btn3Btn4Event()
        // 当start和stop按下的事件
        btn1Btn2Event()
    }

    /**
     * 初始化监视器
     */
    private fun initObserver() {
        lifecycleScope.launch {
            launch {
                appViewModel.received.collect {
                    onRecCmdChanged(it)
                }
            }
            launch {
                viewModel.errorMessage.collect {
                    if (it.contains("A") && it.contains("B")) {
                        stop(X)
                        stop(Y)
                    } else {
                        if (it.contains("A")) {
                            stop(X)
                        }
                        if (it.contains("B")) {
                            stop(Y)
                        }
                    }
                    MessageDialog.build()
                        .setTitle("模块异常")
                        .setMessage(it)
                        .setOkButton("确定") { dialog, _ ->
                            dialog.dismiss()
                            true
                        }
                        .show()
                }
            }
            launch {
                viewModel.getAllProgram().collect {
                    programListA.clear()
                    programListB.clear()
                    it.forEach { program ->
                        if (program.model == 0) {
                            programListA.add(program)
                        } else {
                            programListB.add(program)
                        }
                    }
                    if (!state.isRunX && !state.isRunY) {
                        reloadSpinnerItem(X)
                        reloadSpinnerItem(Y)
                    }
                }
            }
        }
    }

    /**
     * 返回数据变化
     */
    @SuppressLint("SetTextI18n")
    private fun onRecCmdChanged(v1: V1) {
        binding.cmd = v1
        // 运行时禁用开始按钮
        if (state.isRunX || !state.isCanStartX() || v1.inputSensorX == 0) {
            binding.moduleX.btn2.isEnabled = false
            binding.moduleX.btn2.setBackgroundResource(R.drawable.btn_ban)
        }
        if (state.isRunY || !state.isCanStartY() || v1.inputSensorY == 0) {
            binding.moduleY.btn2.isEnabled = false
            binding.moduleY.btn2.setBackgroundResource(R.drawable.btn_ban)
        }

        // moduleX
        if (v1.inputSensorX == 0) {
            binding.moduleX.con5.setBackgroundResource(R.drawable.tv_error)
            binding.moduleX.con6.setBackgroundResource(R.drawable.tv_health)
            binding.moduleX.con7.setBackgroundResource(R.drawable.tv_health)
            binding.moduleX.con8.setBackgroundResource(R.drawable.tv_health)
            if (binding.moduleX.tv5.text.toString() == "已完成") {
                binding.moduleX.tv5.text = "00:00"
            }
        } else {
            binding.moduleX.con5.setBackgroundResource(R.drawable.tv_health)
            if (v1.stepMotorX == 0 && state.isRunX && state.modelX == A) {
                binding.moduleX.con6.setBackgroundResource(R.drawable.tv_error)
            } else {
                binding.moduleX.con6.setBackgroundResource(R.drawable.tv_health)
            }
            if (v1.getVoltageX > v1.targetVoltageX + 1 || v1.getVoltageX < v1.targetVoltageX - 1) {
                binding.moduleX.con7.setBackgroundResource(R.drawable.tv_warning)
            } else {
                binding.moduleX.con7.setBackgroundResource(R.drawable.tv_health)
            }
            if (v1.getCurrentX < 0.1f && v1.getCurrentX > 0) {
                binding.moduleX.con8.setBackgroundResource(R.drawable.tv_warning)
            } else {
                binding.moduleX.con8.setBackgroundResource(R.drawable.tv_health)
            }
            if (state.isCanStartX() && !state.isRunX) {
                binding.moduleX.btn2.isEnabled = true
                binding.moduleX.btn2.setBackgroundResource(R.drawable.btn_press_selector)
            }
        }
        // moduleY
        if (v1.inputSensorY == 0) {
            binding.moduleY.con5.setBackgroundResource(R.drawable.tv_error)
            binding.moduleY.con6.setBackgroundResource(R.drawable.tv_health)
            binding.moduleY.con7.setBackgroundResource(R.drawable.tv_health)
            binding.moduleY.con8.setBackgroundResource(R.drawable.tv_health)
            if (binding.moduleY.tv5.text.toString() == "已完成") {
                binding.moduleY.tv5.text = "00:00"
            }
        } else {
            binding.moduleY.con5.setBackgroundResource(R.drawable.tv_health)
            if (v1.stepMotorY == 0 && state.isRunY && state.modelY === A) {
                binding.moduleY.con6.setBackgroundResource(R.drawable.tv_error)
            } else {
                binding.moduleY.con6.setBackgroundResource(R.drawable.tv_health)
            }
            if (v1.getVoltageY > v1.targetVoltageY + 1 || v1.getVoltageY < v1.targetVoltageY - 1) {
                binding.moduleY.con7.setBackgroundResource(R.drawable.tv_warning)
            } else {
                binding.moduleY.con7.setBackgroundResource(R.drawable.tv_health)
            }
            if (v1.getCurrentY < 0.1f && v1.getCurrentY > 0) {
                binding.moduleY.con8.setBackgroundResource(R.drawable.tv_warning)
            } else {
                binding.moduleY.con8.setBackgroundResource(R.drawable.tv_health)
            }
            if (state.isCanStartY() && !state.isRunY) {
                binding.moduleY.btn2.isEnabled = true
                binding.moduleY.btn2.setBackgroundResource(R.drawable.btn_press_selector)
            }
        }
    }

    /**
     * 选择模式
     */
    private fun tab1Event() {
        binding.moduleX.tab1.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0 && !state.isRunX) {
                    state = state.copy(modelX = A)
                    reloadSpinnerItem(X)
                    // 转膜
                    binding.moduleX.conPump.visibility = View.VISIBLE
                    binding.moduleX.et1.hint = "转速：RPM"
                    if (!state.isRunX) {
                        binding.moduleX.et1.isEnabled = true
                    }
                }
                if (tab.position == 1 && !state.isRunX) {
                    state = state.copy(modelX = B)
                    reloadSpinnerItem(X)
                    // 染色
                    binding.moduleX.conPump.visibility = View.GONE
                    binding.moduleX.et1.setText("")
                    binding.moduleX.et1.hint = "/"
                    binding.moduleX.et1.isEnabled = false
                    state = state.copy(motorX = 0)
                }
                (if (state.modelX === A) {
                    binding.moduleX.tab1.getTabAt(0)
                } else {
                    binding.moduleX.tab1.getTabAt(1)
                })?.select()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        binding.moduleY.tab1.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0 && !state.isRunY) {
                    state = state.copy(modelY = A)
                    reloadSpinnerItem(Y)
                    // 染色
                    binding.moduleY.conPump.visibility = View.VISIBLE
                    binding.moduleY.et1.hint = "转速：RPM"
                    if (!state.isRunY) {
                        binding.moduleY.et1.isEnabled = true
                    }
                }
                if (tab.position == 1 && !state.isRunY) {
                    state = state.copy(modelY = B)
                    reloadSpinnerItem(Y)
                    // 染色
                    binding.moduleY.conPump.visibility = View.GONE
                    binding.moduleY.et1.setText("")
                    binding.moduleY.et1.hint = "/"
                    binding.moduleY.et1.isEnabled = false
                    state = state.copy(motorY = 0)
                }
                (if (state.modelY === A) {
                    binding.moduleY.tab1.getTabAt(0)
                } else {
                    binding.moduleY.tab1.getTabAt(1)
                })?.select()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    /**
     * 给输入参数加单位
     */
    @SuppressLint("SetTextI18n")
    private fun et1Et2Et3Event() {
        // moduleX
        binding.moduleX.et1.addSuffix(" RPM")
        binding.moduleX.et2.addSuffix(" V")
        binding.moduleX.et3.addSuffix(" MIN")
        binding.moduleX.et1.afterTextChange {
            val str = it.replace(" RPM", "").removeZero()
            state = if (str.isNotEmpty()) {
                val motor = str.toInt()
                if (motor > 250) {
                    binding.moduleX.et1.setText("250")
                    state.copy(motorX = 250)
                } else {
                    state.copy(motorX = motor)
                }
            } else {
                state.copy(motorX = 0)
            }
        }
        binding.moduleX.et2.afterTextChange {
            val str = it.replace(" V", "").removeZero()
            state = if (str.isNotEmpty()) {
                val vol = str.toFloat()
                if (vol > 65f) {
                    binding.moduleX.et2.setText("65")
                    state.copy(voltageX = 65f)
                } else {
                    state.copy(voltageX = vol)
                }
            } else {
                state.copy(voltageX = 0f)
            }
        }
        binding.moduleX.et3.afterTextChange {
            val str = it.replace(" MIN", "").removeZero()
            state = if (str.isNotEmpty()) {
                val time = str.toFloat()
                if (time > 99f) {
                    binding.moduleX.et3.setText("99")
                    state.copy(timeX = (99f * 60).toInt())
                } else {
                    state.copy(timeX = (time * 60).toInt())
                }
            } else {
                state.copy(timeX = 0)
            }
        }

        // moduleY
        binding.moduleY.et1.addSuffix(" RPM")
        binding.moduleY.et2.addSuffix(" V")
        binding.moduleY.et3.addSuffix(" MIN")
        binding.moduleY.et1.afterTextChange {
            val str = it.replace(" RPM", "").removeZero()
            state = if (str.isNotEmpty()) {
                val motor = str.toInt()
                if (motor > 250) {
                    binding.moduleY.et1.setText("250")
                    state.copy(motorY = 250)
                } else {
                    state.copy(motorY = motor)
                }
            } else {
                state.copy(motorY = 0)
            }
        }
        binding.moduleY.et2.afterTextChange {
            val str = it.replace(" V", "").removeZero()
            state = if (str.isNotEmpty()) {
                val vol = str.toFloat()
                if (vol > 65f) {
                    binding.moduleY.et2.setText("65")
                    state.copy(voltageY = 65f)
                } else {
                    state.copy(voltageY = vol)
                }
            } else {
                state.copy(voltageY = 0f)
            }
        }
        binding.moduleY.et3.afterTextChange {
            val str = it.replace(" MIN", "").removeZero()
            state = if (str.isNotEmpty()) {
                val time = str.toFloat()
                if (time > 99f) {
                    binding.moduleY.et3.setText("99")
                    state.copy(timeY = (99f * 60).toInt())
                } else {
                    state.copy(timeY = (time * 60).toInt())
                }
            } else {
                state.copy(timeY = 0)
            }
        }

    }

    /**
     * 当start和stop按下的事件
     */
    @SuppressLint("SetTextI18n")
    private fun btn1Btn2Event() {
        // moduleX
        binding.moduleX.btn2.setOnClickListener { start(X) }
        binding.moduleX.btn1.setOnClickListener { stop(X) }
        // moduleY
        binding.moduleY.btn2.setOnClickListener { start(Y) }
        binding.moduleY.btn1.setOnClickListener { stop(Y) }
    }

    /**
     * 当填充和回吸按下的时候
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun btn3Btn4Event() {
        binding.moduleX.btn3.addTouchEvent({
            val sendCmd = appViewModel.send.value
            it.setBackgroundResource(R.drawable.btn_press_shape)
            it.scaleX = 0.8f
            it.scaleY = 0.8f
            state = state.copy(stepMotorX = sendCmd.stepMotorX)
            sendCmd.stepMotorX = appViewModel.setting.value.motorSpeed
            appViewModel.send(sendCmd)
        }, {
            val sendCmd = appViewModel.send.value
            it.setBackgroundResource(R.drawable.btn_nopress_shape)
            it.scaleX = 1f
            it.scaleY = 1f
            sendCmd.stepMotorX = state.stepMotorX
            appViewModel.send(sendCmd)
        })
        binding.moduleX.btn4.addTouchEvent({
            val sendCmd = appViewModel.send.value
            it.setBackgroundResource(R.drawable.btn_press_shape)
            it.scaleX = 0.8f
            it.scaleY = 0.8f
            state = state.copy(stepMotorX = sendCmd.stepMotorX)
            sendCmd.stepMotorX = -appViewModel.setting.value.motorSpeed
            appViewModel.send(sendCmd)
        }, {
            val sendCmd = appViewModel.send.value
            it.setBackgroundResource(R.drawable.btn_nopress_shape)
            it.scaleX = 1f
            it.scaleY = 1f
            sendCmd.stepMotorX = state.stepMotorX
            appViewModel.send(sendCmd)
        })
        binding.moduleY.btn3.addTouchEvent({
            val sendCmd = appViewModel.send.value
            it.setBackgroundResource(R.drawable.btn_press_shape)
            it.scaleX = 0.8f
            it.scaleY = 0.8f
            state = state.copy(stepMotorY = sendCmd.stepMotorY)
            sendCmd.stepMotorY = appViewModel.setting.value.motorSpeed
            appViewModel.send(sendCmd)
        }, {
            val sendCmd = appViewModel.send.value
            it.setBackgroundResource(R.drawable.btn_nopress_shape)
            it.scaleX = 1f
            it.scaleY = 1f
            sendCmd.stepMotorY = state.stepMotorY
            appViewModel.send(sendCmd)
        })
        binding.moduleY.btn4.addTouchEvent({
            val sendCmd = appViewModel.send.value
            it.setBackgroundResource(R.drawable.btn_press_shape)
            it.scaleX = 0.8f
            it.scaleY = 0.8f
            state = state.copy(stepMotorY = sendCmd.stepMotorY)
            sendCmd.stepMotorY = -appViewModel.setting.value.motorSpeed
            appViewModel.send(sendCmd)
        }, {
            val sendCmd = appViewModel.send.value
            it.setBackgroundResource(R.drawable.btn_nopress_shape)
            it.scaleX = 1f
            it.scaleY = 1f
            sendCmd.stepMotorY = state.stepMotorY
            appViewModel.send(sendCmd)
        })
    }

    /**
     * 开始按钮按下
     * @param module 模块
     */
    private fun start(module: Model) {
        var time = 0
        disableEtSp(module)
        sendStartCmd(module)
        when (module) {
            X -> {
                setProgramAndLog(X, state.modelX)
                if (!state.isRunY && appViewModel.setting.value.detect) {
                    viewModel.setSentinel()
                }
                if (!state.isRunY && state.modelX === A) {
                    autoClean()
                }
                xDisposable?.run {
                    if (this.isDisposed.not()) {
                        this.dispose()
                    }
                }
                time = state.timeX
            }

            Y -> {
                setProgramAndLog(Y, state.modelY)
                if (!state.isRunX && appViewModel.setting.value.detect) {
                    viewModel.setSentinel()
                }
                if (!state.isRunX && state.modelY === A) {
                    autoClean()
                }
                yDisposable?.run {
                    if (this.isDisposed.not()) {
                        this.dispose()
                    }
                }
                time = state.timeY
            }

            else -> {}
        }
        Observable.intervalRange(0, time.toLong(), 0, 1, TimeUnit.SECONDS)
            .map { aLong: Long -> time - aLong - 1 }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<Long> {
                override fun onSubscribe(d: Disposable) {
                    if (module == X) {
                        xDisposable = d
                    }
                    if (module == Y) {
                        yDisposable = d
                    }
                }

                override fun onNext(aLong: Long) {
                    if (module == X) {
                        binding.moduleX.tv5.text = aLong.getTimeFormat()
                    }
                    if (module == Y) {
                        binding.moduleY.tv5.text = aLong.getTimeFormat()
                    }
                }

                override fun onError(e: Throwable) {}

                override fun onComplete() {
                    enableEtSp(module)
                    sendStopCmd(module)
                    viewModel.stopRecordLog(module)
                    viewModel.playAudio(R.raw.finish)
                    if (module == X) {
                        binding.moduleX.tv5.text = "已完成"
                        Logger.d(msg = "模块A计时停止")
                        if (!state.isRunY) {
                            zDisposable?.run {
                                if (this.isDisposed.not()) {
                                    this.dispose()
                                }
                            }
                        }
                    }
                    if (module == Y) {
                        binding.moduleY.tv5.text = "已完成"
                        Logger.d(msg = "模块B计时停止")
                        if (!state.isRunX) {
                            zDisposable?.run {
                                if (this.isDisposed.not()) {
                                    this.dispose()
                                }
                            }
                        }
                    }
                }
            })
    }

    /**
     * 停止按钮按下
     * @param module 模块
     */
    @SuppressLint("SetTextI18n")
    private fun stop(module: Model) {
        enableEtSp(module)
        sendStopCmd(module)
        viewModel.stopRecordLog(module)
        if (module == X) {
            xDisposable?.run {
                if (this.isDisposed.not()) {
                    this.dispose()
                }
            }
            binding.moduleX.tv5.text = "00:00"
            if (!state.isRunY) {
                zDisposable?.run {
                    if (this.isDisposed.not()) {
                        this.dispose()
                    }
                }
            }
            Logger.d(msg = "模块A停止")
        }
        if (module == Y) {
            yDisposable?.run {
                if (this.isDisposed.not()) {
                    this.dispose()
                }
            }
            binding.moduleY.tv5.text = "00:00"
            if (!state.isRunX) {
                zDisposable?.run {
                    if (this.isDisposed.not()) {
                        this.dispose()
                    }
                }
            }
            Logger.d(msg = "模块B停止")
        }
    }

    /**
     * 自动清理费液
     */
    private fun autoClean() {
        zDisposable?.run {
            if (this.isDisposed.not()) {
                this.dispose()
            }
        }
        Observable.interval(
            appViewModel.setting.value.interval.toLong(),
            appViewModel.setting.value.interval.toLong(),
            TimeUnit.MINUTES
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<Long> {
                override fun onSubscribe(d: Disposable) {
                    zDisposable = d
                }

                override fun onNext(aLong: Long) {
                    val sendCmd = appViewModel.send.value
                    sendCmd.motorX = 1
                    sendCmd.motorY = 1
                    appViewModel.send(sendCmd)
                    Logger.d(msg = "开始定时自动清理废液")
                    lifecycleScope.launch {
                        delay(appViewModel.setting.value.duration.toLong() * 1000)
                        val cmd = appViewModel.send.value
                        cmd.motorX = 0
                        cmd.motorY = 0
                        appViewModel.send(cmd)
                        Logger.d(msg = "结束定时自动清理废液")
                    }
                }

                override fun onError(e: Throwable) {}
                override fun onComplete() {}
            })
    }

    /**
     * 不可编辑，开始按钮按下后
     * @param module 模块
     */
    private fun disableEtSp(module: Model) {
        if (module === X) {
            binding.moduleX.sp1.isEnabled = false
            if (state.modelX === A) {
                binding.moduleX.et1.isEnabled = false
            }
            binding.moduleX.et2.isEnabled = false
            binding.moduleX.et3.isEnabled = false
        }
        if (module === Y) {
            binding.moduleY.sp1.isEnabled = false
            if (state.modelY === A) {
                binding.moduleY.et1.isEnabled = false
            }
            binding.moduleY.et2.isEnabled = false
            binding.moduleY.et3.isEnabled = false
        }
    }


    /**
     * 可编辑，开始按钮按下后
     * @param module 模块
     */
    private fun enableEtSp(module: Model) {
        if (module === X) {
            binding.moduleX.sp1.isEnabled = true
            if (state.modelX === A) {
                binding.moduleX.et1.isEnabled = true
            }
            binding.moduleX.et2.isEnabled = true
            binding.moduleX.et3.isEnabled = true
        }
        if (module === Y) {
            binding.moduleY.sp1.isEnabled = true
            if (state.modelY === A) {
                binding.moduleY.et1.isEnabled = true
            }
            binding.moduleY.et2.isEnabled = true
            binding.moduleY.et3.isEnabled = true
        }
    }

    /**
     * 发送开始命令
     * @param module 模块
     */
    private fun sendStartCmd(module: Model) {
        val sendCmd = appViewModel.send.value
        if (module == X) {
            state = state.copy(isRunX = true)
            sendCmd.powerENX = 1
            sendCmd.autoX = 1
            if (state.modelX === A) {
                sendCmd.stepMotorX = state.motorX
            } else {
                sendCmd.stepMotorX = 0
            }
            sendCmd.targetVoltageX = state.voltageX
        }
        if (module == Y) {
            state = state.copy(isRunY = true)
            sendCmd.powerENY = 1
            sendCmd.autoY = 1
            if (state.modelY === A) {
                sendCmd.stepMotorY = state.motorY
            } else {
                sendCmd.stepMotorY = 0
            }
            sendCmd.targetVoltageY = state.voltageY
        }
        appViewModel.send(sendCmd)
    }

    /**
     * 发送停止命令
     * @param module 模块
     */
    private fun sendStopCmd(module: Model) {
        val sendCmd = appViewModel.send.value
        if (module == X) {
            state = state.copy(isRunX = false)
            sendCmd.powerENX = 0
            sendCmd.autoX = 0
            sendCmd.stepMotorX = 0
            sendCmd.targetVoltageX = 0f
        }
        if (module == Y) {
            state = state.copy(isRunY = false)
            sendCmd.powerENY = 0
            sendCmd.autoY = 0
            sendCmd.stepMotorY = 0
            sendCmd.targetVoltageY = 0f
        }
        appViewModel.send(sendCmd)
    }

    /**
     * 初始化选项框
     */
    @SuppressLint("SetTextI18n")
    private fun initSp() {
        adapterX =
            SpinnerAdapter(requireContext(), programListX, "name", Program::class.java)
        adapterY =
            SpinnerAdapter(requireContext(), programListY, "name", Program::class.java)
        binding.moduleX.sp1.adapter = adapterX
        binding.moduleY.sp1.adapter = adapterY
        binding.moduleX.sp1.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View,
                    i: Int,
                    l: Long
                ) {
                    val program = adapterX.getItem(i)
                    if (state.modelX === A) {
                        binding.moduleX.et1.setText(program.motor.toString().removeZero() + " RPM")
                        state = state.copy(motorX = program.motor)
                    }
                    binding.moduleX.et2.setText(program.voltage.toString().removeZero() + " V")
                    state = state.copy(voltageX = program.voltage)
                    binding.moduleX.et3.setText(program.time.toString().removeZero() + " MIN")
                    state = state.copy(timeX = (program.time * 60).toInt())
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            }
        binding.moduleY.sp1.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View,
                    i: Int,
                    l: Long
                ) {
                    val program = adapterY.getItem(i)
                    if (state.modelY === A) {
                        binding.moduleY.et1.setText(program.motor.toString().removeZero() + " RPM")
                        state = state.copy(motorY = program.motor)
                    }
                    binding.moduleY.et2.setText(program.voltage.toString().removeZero() + " V")
                    state = state.copy(voltageY = program.voltage)
                    binding.moduleY.et3.setText(program.time.toString().removeZero() + " MIN")
                    state = state.copy(timeY = (program.time * 60).toInt())
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            }
    }

    /**
     * 重新加载选项框
     * @param module 模块
     */
    @SuppressLint("SetTextI18n")
    private fun reloadSpinnerItem(module: Model) {
        if (module === X) {
            programListX.clear()
            if (state.modelX === A) {
                programListX.addAll(programListA)
            }
            if (state.modelX === B) {
                programListX.addAll(programListB)
            }
            adapterX.notifyDataSetChanged()
            if (programListX.isNotEmpty()) {
                var program = programListX[0]
                programListX.forEachIndexed { index, p ->
                    if (p.def == 1) {
                        program = p
                        binding.moduleX.sp1.setSelection(index)
                    }
                }
                if (state.modelX === A) {
                    binding.moduleX.et1.setText(program.motor.toString().removeZero() + " RPM")
                    state = state.copy(motorX = program.motor)
                }
                binding.moduleX.et2.setText(program.voltage.toString().removeZero() + " V")
                state = state.copy(voltageX = program.voltage)
                binding.moduleX.et3.setText(program.time.toString().removeZero() + " MIN")
                state = state.copy(timeX = (program.time * 60).toInt())
            } else {
                binding.moduleX.et1.setText("")
                state = state.copy(motorX = 0)
                binding.moduleX.et2.setText("")
                state = state.copy(voltageX = 0f)
                binding.moduleX.et3.setText("")
                state = state.copy(timeX = 0)
            }

        }
        if (module === Y) {
            programListY.clear()
            if (state.modelY === A) {
                programListY.addAll(programListA)
            }
            if (state.modelY === B) {
                programListY.addAll(programListB)
            }
            adapterY.notifyDataSetChanged()
            if (programListY.isNotEmpty()) {
                var program = programListY[0]
                programListY.forEachIndexed { index, p ->
                    if (p.def == 1) {
                        program = p
                        binding.moduleY.sp1.setSelection(index)
                    }
                }
                if (state.modelY === A) {
                    binding.moduleY.et1.setText(program.motor.toString().removeZero() + " RPM")
                    state = state.copy(motorY = program.motor)
                }
                binding.moduleY.et2.setText(program.voltage.toString().removeZero() + " V")
                state = state.copy(voltageY = program.voltage)
                binding.moduleY.et3.setText(program.time.toString().removeZero() + " MIN")
                state = state.copy(timeY = (program.time * 60).toInt())
            } else {
                binding.moduleY.et1.setText("")
                state = state.copy(motorY = 0)
                binding.moduleY.et2.setText("")
                state = state.copy(voltageY = 0f)
                binding.moduleY.et3.setText("")
                state = state.copy(timeY = 0)
            }
        }
    }

    /**
     * 把当前选定的配置设为默认
     * @param module 模块
     * @param model 模式
     */
    private fun setProgramAndLog(module: Model, model: Model) {
        lifecycleScope.launch {
            viewModel.updateProgramDefaultByKind(if (model === A) 0 else 1)
            var program: Program? = null
            if (module === X && programListX.isNotEmpty()) {
                program = adapterX.getItem(binding.moduleX.sp1.selectedItemPosition)
            }
            if (module === Y && programListY.isNotEmpty()) {
                program = adapterY.getItem(binding.moduleY.sp1.selectedItemPosition)
            }
            if (program != null && program.name.isNotEmpty()) {
                viewModel.updateProgram(
                    program.copy(
                        def = 1,
                        count = program.count + 1,
                        upload = 0
                    )
                )
                viewModel.startRecordLog(module, program.id, state)
            } else {
                viewModel.startRecordLog(module, "no-program", state)
            }
        }
    }

}