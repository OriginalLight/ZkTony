package com.zktony.www.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.kongzue.dialogx.dialogs.MessageDialog
import com.zktony.serialport.COMSerial
import com.zktony.www.R
import com.zktony.www.adapter.SpinnerAdapter
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.AppLog
import com.zktony.www.common.app.AppIntent
import com.zktony.www.common.app.AppState
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.constant.Constants
import com.zktony.www.common.extension.addSuffix
import com.zktony.www.common.extension.addTouchEvent
import com.zktony.www.common.extension.afterTextChange
import com.zktony.www.common.extension.getTimeFormat
import com.zktony.www.common.extension.removeZero
import com.zktony.www.common.model.Event
import com.zktony.www.common.model.SerialPort
import com.zktony.www.data.entity.Program
import com.zktony.www.databinding.FragmentHomeBinding
import com.zktony.www.ui.home.model.Cmd
import com.zktony.www.ui.home.model.ControlState
import com.zktony.www.ui.home.model.Model
import com.zktony.www.ui.home.model.Model.A
import com.zktony.www.ui.home.model.Model.B
import com.zktony.www.ui.home.model.Model.X
import com.zktony.www.ui.home.model.Model.Y
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>(R.layout.fragment_home) {

    override val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var appViewModel: AppViewModel

    lateinit var spinnerAdapterX: SpinnerAdapter<Program>
    lateinit var spinnerAdapterY: SpinnerAdapter<Program>
    private val programListA = arrayListOf<Program>()
    private val programListB = arrayListOf<Program>()
    private val programListX = arrayListOf<Program>()
    private val programListY = arrayListOf<Program>()

    private val state = ControlState()
    private var xDisposable: Disposable? = null
    private var yDisposable: Disposable? = null
    private var zDisposable: Disposable? = null

    override fun onViewCreated(savedInstanceState: Bundle?) {
        EventBus.getDefault().register(this)
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

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    /**
     * 初始化监视器
     */
    private fun initObserver() {
        viewModel.initQueryWork()
        lifecycleScope.launch {
            appViewModel.state.collect {
                when (it) {
                    is AppState.SendCmd -> {
                        COMSerial.instance.sendHex(SerialPort.TTYS4.device, it.cmd.genHex())
                    }

                    is AppState.ReceiveCmd -> onRecCmdChanged(it.cmd)
                }
            }
        }
        lifecycleScope.launch {
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
        lifecycleScope.launch {
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

    /**
     * 返回数据变化
     */
    @SuppressLint("SetTextI18n")
    private fun onRecCmdChanged(cmd: Cmd) {
        binding.cmd = cmd
        // 运行时禁用开始按钮
        if (state.isRunX || !state.isCanStartX || cmd.inputSensorX == 0) {
            binding.moduleX.btn2.isEnabled = false
            binding.moduleX.btn2.setBackgroundResource(R.drawable.btn_ban)
        }
        if (state.isRunY || !state.isCanStartY || cmd.inputSensorY == 0) {
            binding.moduleY.btn2.isEnabled = false
            binding.moduleY.btn2.setBackgroundResource(R.drawable.btn_ban)
        }

        // moduleX
        if (cmd.inputSensorX == 0) {
            binding.moduleX.con5.setBackgroundResource(R.drawable.tv_error)
            binding.moduleX.con6.setBackgroundResource(R.drawable.tv_health)
            binding.moduleX.con7.setBackgroundResource(R.drawable.tv_health)
            binding.moduleX.con8.setBackgroundResource(R.drawable.tv_health)
            if (binding.moduleX.tv5.text.toString() == "已完成") {
                binding.moduleX.tv5.text = "00:00"
            }
        } else {
            binding.moduleX.con5.setBackgroundResource(R.drawable.tv_health)
            if (cmd.stepMotorX == 0 && state.isRunX && state.modelX == A) {
                binding.moduleX.con6.setBackgroundResource(R.drawable.tv_error)
            } else {
                binding.moduleX.con6.setBackgroundResource(R.drawable.tv_health)
            }
            if (cmd.getVoltageX > cmd.targetVoltageX + 1 || cmd.getVoltageX < cmd.targetVoltageX - 1) {
                binding.moduleX.con7.setBackgroundResource(R.drawable.tv_warning)
            } else {
                binding.moduleX.con7.setBackgroundResource(R.drawable.tv_health)
            }
            if (cmd.getCurrentX < 0.1f && cmd.getCurrentX > 0) {
                binding.moduleX.con8.setBackgroundResource(R.drawable.tv_warning)
            } else {
                binding.moduleX.con8.setBackgroundResource(R.drawable.tv_health)
            }
            if (state.isCanStartX && !state.isRunX) {
                binding.moduleX.btn2.isEnabled = true
                binding.moduleX.btn2.setBackgroundResource(R.drawable.btn_press_selector)
            }
        }
        // moduleY
        if (cmd.inputSensorY == 0) {
            binding.moduleY.con5.setBackgroundResource(R.drawable.tv_error)
            binding.moduleY.con6.setBackgroundResource(R.drawable.tv_health)
            binding.moduleY.con7.setBackgroundResource(R.drawable.tv_health)
            binding.moduleY.con8.setBackgroundResource(R.drawable.tv_health)
            if (binding.moduleY.tv5.text.toString() == "已完成") {
                binding.moduleY.tv5.text = "00:00"
            }
        } else {
            binding.moduleY.con5.setBackgroundResource(R.drawable.tv_health)
            if (cmd.stepMotorY == 0 && state.isRunY && state.modelY === A) {
                binding.moduleY.con6.setBackgroundResource(R.drawable.tv_error)
            } else {
                binding.moduleY.con6.setBackgroundResource(R.drawable.tv_health)
            }
            if (cmd.getVoltageY > cmd.targetVoltageY + 1 || cmd.getVoltageY < cmd.targetVoltageY - 1) {
                binding.moduleY.con7.setBackgroundResource(R.drawable.tv_warning)
            } else {
                binding.moduleY.con7.setBackgroundResource(R.drawable.tv_health)
            }
            if (cmd.getCurrentY < 0.1f && cmd.getCurrentY > 0) {
                binding.moduleY.con8.setBackgroundResource(R.drawable.tv_warning)
            } else {
                binding.moduleY.con8.setBackgroundResource(R.drawable.tv_health)
            }
            if (state.isCanStartY && !state.isRunY) {
                binding.moduleY.btn2.isEnabled = true
                binding.moduleY.btn2.setBackgroundResource(R.drawable.btn_press_selector)
            }
        }
    }

    /**
     * 根据Tab选择模式
     */
    private fun tab1Event() {
        binding.moduleX.tab1.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0 && !state.isRunX) {
                    state.modelX = A
                    reloadSpinnerItem(X)
                    // 转膜
                    binding.moduleX.conPump.visibility = View.VISIBLE
                    binding.moduleX.et1.hint = "转速：RPM"
                    if (!state.isRunX) {
                        binding.moduleX.et1.isEnabled = true
                    }
                }
                if (tab.position == 1 && !state.isRunX) {
                    state.modelX = B
                    reloadSpinnerItem(X)
                    // 染色
                    binding.moduleX.conPump.visibility = View.GONE
                    binding.moduleX.et1.setText("")
                    binding.moduleX.et1.hint = "/"
                    binding.moduleX.et1.isEnabled = false
                    state.motorX = 0
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
                    state.modelY = A
                    reloadSpinnerItem(Y)
                    // 染色
                    binding.moduleY.conPump.visibility = View.VISIBLE
                    binding.moduleY.et1.hint = "转速：RPM"
                    if (!state.isRunY) {
                        binding.moduleY.et1.isEnabled = true
                    }
                }
                if (tab.position == 1 && !state.isRunY) {
                    state.modelY = B
                    reloadSpinnerItem(Y)
                    // 染色
                    binding.moduleY.conPump.visibility = View.GONE
                    binding.moduleY.et1.setText("")
                    binding.moduleY.et1.hint = "/"
                    binding.moduleY.et1.isEnabled = false
                    state.motorY = 0
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
            if (str.isNotEmpty()) {
                val motor = str.toInt()
                if (motor > 250) {
                    binding.moduleX.et1.setText("250")
                    state.motorX = 250
                } else {
                    state.motorX = motor
                }
            } else {
                state.motorX = 0
            }
        }
        binding.moduleX.et2.afterTextChange {
            val str = it.replace(" V", "").removeZero()
            if (str.isNotEmpty()) {
                val vol = str.toFloat()
                if (vol > 65f) {
                    binding.moduleX.et2.setText("65")
                    state.voltageX = 65f
                } else {
                    state.voltageX = vol
                }
            } else {
                state.voltageX = 0f
            }
        }
        binding.moduleX.et3.afterTextChange {
            val str = it.replace(" MIN", "").removeZero()
            if (str.isNotEmpty()) {
                val time = str.toFloat()
                if (time > 99f) {
                    binding.moduleX.et3.setText("99")
                    state.timeX = (99f * 60).toInt()
                } else {
                    state.timeX = (time * 60).toInt()
                }
            } else {
                state.timeX = 0
            }
        }

        // moduleY
        binding.moduleY.et1.addSuffix(" RPM")
        binding.moduleY.et2.addSuffix(" V")
        binding.moduleY.et3.addSuffix(" MIN")
        binding.moduleY.et1.afterTextChange {
            val str = it.replace(" RPM", "").removeZero()
            if (str.isNotEmpty()) {
                val motor = str.toInt()
                if (motor > 250) {
                    binding.moduleY.et1.setText("250")
                    state.motorY = 250
                } else {
                    state.motorY = motor
                }
            } else {
                state.motorY = 0
            }
        }
        binding.moduleY.et2.afterTextChange {
            val str = it.replace(" V", "").removeZero()
            if (str.isNotEmpty()) {
                val vol = str.toFloat()
                if (vol > 65f) {
                    binding.moduleY.et2.setText("65")
                    state.voltageY = 65f
                } else {
                    state.voltageY = vol
                }
            } else {
                state.voltageY = 0f
            }
        }
        binding.moduleY.et3.afterTextChange {
            val str = it.replace(" MIN", "").removeZero()
            if (str.isNotEmpty()) {
                val time = str.toFloat()
                if (time > 99f) {
                    binding.moduleY.et3.setText("99")
                    state.timeY = (99f * 60).toInt()
                } else {
                    state.timeY = (time * 60).toInt()
                }
            } else {
                state.timeY = 0
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
            val sendCmd = appViewModel.latestSendCmd
            it.setBackgroundResource(R.drawable.btn_press_shape)
            it.scaleX = 0.8f
            it.scaleY = 0.8f
            state.stepMotorX = sendCmd.stepMotorX
            sendCmd.stepMotorX = 160
            appViewModel.dispatch(AppIntent.SendCmd(sendCmd))
        }, {
            val sendCmd = appViewModel.latestSendCmd
            it.setBackgroundResource(R.drawable.btn_nopress_shape)
            it.scaleX = 1f
            it.scaleY = 1f
            sendCmd.stepMotorX = state.stepMotorX
            appViewModel.dispatch(AppIntent.SendCmd(sendCmd))
        })
        binding.moduleX.btn4.addTouchEvent({
            val sendCmd = appViewModel.latestSendCmd
            it.setBackgroundResource(R.drawable.btn_press_shape)
            it.scaleX = 0.8f
            it.scaleY = 0.8f
            state.stepMotorX = sendCmd.stepMotorX
            sendCmd.stepMotorX = -160
            appViewModel.dispatch(AppIntent.SendCmd(sendCmd))
        }, {
            val sendCmd = appViewModel.latestSendCmd
            it.setBackgroundResource(R.drawable.btn_nopress_shape)
            it.scaleX = 1f
            it.scaleY = 1f
            sendCmd.stepMotorX = state.stepMotorX
            appViewModel.dispatch(AppIntent.SendCmd(sendCmd))
        })
        binding.moduleY.btn3.addTouchEvent({
            val sendCmd = appViewModel.latestSendCmd
            it.setBackgroundResource(R.drawable.btn_press_shape)
            it.scaleX = 0.8f
            it.scaleY = 0.8f
            state.stepMotorY = sendCmd.stepMotorY
            sendCmd.stepMotorY = 160
            appViewModel.dispatch(AppIntent.SendCmd(sendCmd))
        }, {
            val sendCmd = appViewModel.latestSendCmd
            it.setBackgroundResource(R.drawable.btn_nopress_shape)
            it.scaleX = 1f
            it.scaleY = 1f
            sendCmd.stepMotorY = state.stepMotorY
            appViewModel.dispatch(AppIntent.SendCmd(sendCmd))
        })
        binding.moduleY.btn4.addTouchEvent({
            val sendCmd = appViewModel.latestSendCmd
            it.setBackgroundResource(R.drawable.btn_press_shape)
            it.scaleX = 0.8f
            it.scaleY = 0.8f
            state.stepMotorY = sendCmd.stepMotorY
            sendCmd.stepMotorY = -160
            appViewModel.dispatch(AppIntent.SendCmd(sendCmd))
        }, {
            val sendCmd = appViewModel.latestSendCmd
            it.setBackgroundResource(R.drawable.btn_nopress_shape)
            it.scaleX = 1f
            it.scaleY = 1f
            sendCmd.stepMotorY = state.stepMotorY
            appViewModel.dispatch(AppIntent.SendCmd(sendCmd))
        })
    }

    /**
     * 开始按钮按下
     */
    private fun start(module: Model) {
        var time = 0
        disableEtSp(module)
        sendStartCmd(module)
        when (module) {
            X -> {
                setProgramAndLog(X, state.modelX)
                if (!state.isRunY && viewModel.detect) {
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
                if (!state.isRunX && viewModel.detect) {
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
                    if (module == X) {
                        binding.moduleX.tv5.text = "已完成"
                        EventBus.getDefault().post(
                            Event(
                                Constants.AUDIOID,
                                if (state.modelX === A) R.raw.zm else R.raw.rs
                            )
                        )
                        AppLog.d(msg = "模块A计时停止：")
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
                        EventBus.getDefault().post(
                            Event(
                                Constants.AUDIOID,
                                if (state.modelY === A) R.raw.zm else R.raw.rs
                            )
                        )
                        AppLog.d(msg = "模块B计时停止：")
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
            AppLog.d(msg = "模块A停止：")
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
            AppLog.d(msg = "模块B停止：")
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
            viewModel.interval.toLong(),
            viewModel.interval.toLong(),
            TimeUnit.MINUTES
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<Long> {
                override fun onSubscribe(d: Disposable) {
                    zDisposable = d
                }

                override fun onNext(aLong: Long) {
                    val sendCmd = appViewModel.latestSendCmd
                    sendCmd.zmotorX = 1
                    sendCmd.zmotorY = 1
                    appViewModel.dispatch(AppIntent.SendCmd(sendCmd))
                    AppLog.d(msg = "开始定时自动清理废液")
                    lifecycleScope.launch {
                        delay(viewModel.duration.toLong() * 1000)
                        val cmd = appViewModel.latestSendCmd
                        cmd.zmotorX = 0
                        cmd.zmotorY = 0
                        appViewModel.dispatch(AppIntent.SendCmd(cmd))
                        AppLog.d(msg = "结束定时自动清理废液")
                    }
                }

                override fun onError(e: Throwable) {}
                override fun onComplete() {}
            })
    }

    /**
     * 不可编辑，开始按钮按下后
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
     */
    private fun sendStartCmd(module: Model) {
        val sendCmd = appViewModel.latestSendCmd
        if (module == X) {
            state.isRunX = true
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
            state.isRunY = true
            sendCmd.powerENY = 1
            sendCmd.autoY = 1
            if (state.modelY === A) {
                sendCmd.stepMotorY = state.motorY
            } else {
                sendCmd.stepMotorY = 0
            }
            sendCmd.targetVoltageY = state.voltageY
        }
        appViewModel.dispatch(AppIntent.SendCmd(sendCmd))
    }

    /**
     * 发送停止命令
     */
    private fun sendStopCmd(module: Model) {
        val sendCmd = appViewModel.latestSendCmd
        if (module == X) {
            state.isRunX = false
            sendCmd.powerENX = 0
            sendCmd.autoX = 0
            sendCmd.stepMotorX = 0
            sendCmd.targetVoltageX = 0f
        }
        if (module == Y) {
            state.isRunY = false
            sendCmd.powerENY = 0
            sendCmd.autoY = 0
            sendCmd.stepMotorY = 0
            sendCmd.targetVoltageY = 0f
        }
        appViewModel.dispatch(AppIntent.SendCmd(sendCmd))
    }

    /**
     * 初始化选项框
     */
    @SuppressLint("SetTextI18n")
    private fun initSp() {
        spinnerAdapterX =
            SpinnerAdapter(requireContext(), programListX, "name", Program::class.java)
        spinnerAdapterY =
            SpinnerAdapter(requireContext(), programListY, "name", Program::class.java)
        binding.moduleX.sp1.adapter = spinnerAdapterX
        binding.moduleY.sp1.adapter = spinnerAdapterY
        binding.moduleX.sp1.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View,
                    i: Int,
                    l: Long
                ) {
                    val program = spinnerAdapterX.getItem(i)
                    if (state.modelX === A) {
                        binding.moduleX.et1.setText(program.motor.toString().removeZero() + " RPM")
                        state.motorX = program.motor
                    }
                    binding.moduleX.et2.setText(program.voltage.toString().removeZero() + " V")
                    state.voltageX = program.voltage
                    binding.moduleX.et3.setText(program.time.toString().removeZero() + " MIN")
                    state.timeX = (program.time * 60).toInt()
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
                    val program = spinnerAdapterY.getItem(i)
                    if (state.modelY === A) {
                        binding.moduleY.et1.setText(program.motor.toString().removeZero() + " RPM")
                        state.motorY = program.motor
                    }
                    binding.moduleY.et2.setText(program.voltage.toString().removeZero() + " V")
                    state.voltageY = program.voltage
                    binding.moduleY.et3.setText(program.time.toString().removeZero() + " MIN")
                    state.timeY = (program.time * 60).toInt()
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            }
    }

    /**
     * 重新加载选项框
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
            spinnerAdapterX.notifyDataSetChanged()
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
                    state.motorX = program.motor
                }
                binding.moduleX.et2.setText(program.voltage.toString().removeZero() + " V")
                state.voltageX = program.voltage
                binding.moduleX.et3.setText(program.time.toString().removeZero() + " MIN")
                state.timeX = (program.time * 60).toInt()
            } else {
                binding.moduleX.et1.setText("")
                state.motorX = 0
                binding.moduleX.et2.setText("")
                state.voltageX = 0f
                binding.moduleX.et3.setText("")
                state.timeX = 0
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
            spinnerAdapterY.notifyDataSetChanged()
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
                    state.motorY = program.motor
                }
                binding.moduleY.et2.setText(program.voltage.toString().removeZero() + " V")
                state.voltageY = program.voltage
                binding.moduleY.et3.setText(program.time.toString().removeZero() + " MIN")
                state.timeY = (program.time * 60).toInt()
            } else {
                binding.moduleY.et1.setText("")
                state.motorY = 0
                binding.moduleY.et2.setText("")
                state.voltageY = 0f
                binding.moduleY.et3.setText("")
                state.timeY = 0
            }
        }
    }

    /**
     * 把当前选定的配置设为默认
     */
    private fun setProgramAndLog(module: Model, model: Model) {
        lifecycleScope.launch {
            viewModel.updateProgramDefaultByKind(if (model === A) 0 else 1)
            var program: Program? = null
            if (module === X && programListX.isNotEmpty()) {
                program = spinnerAdapterX.getItem(binding.moduleX.sp1.selectedItemPosition)
            }
            if (module === Y && programListY.isNotEmpty()) {
                program = spinnerAdapterY.getItem(binding.moduleY.sp1.selectedItemPosition)
            }
            if (program != null && program.name.isNotEmpty()) {
                program.def = 1
                program.count = program.count + 1
                program.upload = 0
                viewModel.updateProgram(program)
                viewModel.startRecordLog(module, program.id, state)
            } else {
                viewModel.startRecordLog(module, "no-program", state)
            }
        }
    }

    /**
     * 使用EventBus来线程间通信
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGetMessage(event: Event<String, String>) {
        if (Constants.RESET == event.message) {
            stop(X)
            stop(Y)
        }
    }
}