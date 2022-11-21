package com.zktony.www.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.extension.clickScale
import com.zktony.www.databinding.FragmentHomeBinding
import com.zktony.www.serialport.SerialPortManager
import com.zktony.www.ui.home.ModuleEnum.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>(R.layout.fragment_home) {

    override val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var appViewModel: AppViewModel

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initObserver()
        initTextView()
        initButton()
    }

    /**
     * 初始化观察者
     */
    private fun initObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.stateOne.collect { moduleStateChange(A, it) } }
                launch { viewModel.stateTwo.collect { moduleStateChange(B, it) } }
                launch { viewModel.stateThree.collect { moduleStateChange(C, it) } }
                launch { viewModel.stateFour.collect { moduleStateChange(D, it) } }
                launch { viewModel.stateOperating.collect { operationStateChange(it) } }
            }
        }
    }

    /**
     * 初始化文本框
     */
    @SuppressLint("SetTextI18n")
    private fun initTextView() {
        binding.a.tvActions.setOnClickListener { PopTip.show((it as TextView).text) }
        binding.b.tvActions.setOnClickListener { PopTip.show((it as TextView).text) }
        binding.c.tvActions.setOnClickListener { PopTip.show((it as TextView).text) }
        binding.d.tvActions.setOnClickListener { PopTip.show((it as TextView).text) }
    }

    /**
     * 初始化按钮
     */
    private fun initButton() {
        binding.a.run {
            btnStart.setOnClickListener { this@HomeFragment.viewModel.start(A) }
            btnStop.run {
                setOnClickListener { PopTip.show(R.mipmap.ic_stop, "长按停止") }
                setOnLongClickListener {
                    this@HomeFragment.viewModel.stop(A)
                    true
                }
            }
            btnProgram.setOnClickListener { showSelectProgramDialog(A) }
        }
        binding.b.run {
            btnStart.setOnClickListener { this@HomeFragment.viewModel.start(B) }
            btnStop.run {
                setOnClickListener { PopTip.show(R.mipmap.ic_stop, "长按停止") }
                setOnLongClickListener {
                    this@HomeFragment.viewModel.stop(B)
                    true
                }
            }
            btnProgram.setOnClickListener { showSelectProgramDialog(B) }
        }
        binding.c.run {
            btnStart.setOnClickListener { this@HomeFragment.viewModel.start(C) }
            btnStop.run {
                setOnClickListener {
                    PopTip.show(R.mipmap.ic_stop, "长按停止")
                }
                setOnLongClickListener {
                    this@HomeFragment.viewModel.stop(C)
                    true
                }
            }
            btnProgram.setOnClickListener { showSelectProgramDialog(C) }
        }
        binding.d.run {
            btnStart.setOnClickListener { this@HomeFragment.viewModel.start(D) }
            btnStop.run {
                setOnClickListener {
                    PopTip.show(R.mipmap.ic_stop, "长按停止")
                }
                setOnLongClickListener {
                    this@HomeFragment.viewModel.stop(D)
                    true
                }
            }
            btnProgram.setOnClickListener { showSelectProgramDialog(D) }
        }
        binding.e.run {
            btnReset.run {
                clickScale()
                setOnClickListener { PopTip.show(R.mipmap.ic_reset, "长按复位") }
                setOnLongClickListener {
                    this@HomeFragment.viewModel.reset()
                    true
                }
            }
            btnPause.run {
                clickScale()
                setOnClickListener {
                    if (this@HomeFragment.viewModel.stateOperating.value.pauseEnable) {
                        PopTip.show(R.mipmap.ic_stop, "已继续摇床，再次点击暂停")
                    } else {
                        PopTip.show(R.mipmap.ic_stop, "已暂停摇床，再次点击继续")
                    }
                    this@HomeFragment.viewModel.pause()
                }
            }
            btnInsulating.run {
                clickScale()
                setOnClickListener {
                    if (this@HomeFragment.viewModel.stateOperating.value.insulatingEnable) {
                        PopTip.show(R.mipmap.ic_insulating, "已取消保温，再次点击开启")
                    } else {
                        PopTip.show(R.mipmap.ic_insulating, "抗体保温中，再次点击取消")
                    }
                    this@HomeFragment.viewModel.insulating()
                }
            }
        }
    }

    /**
     * 各个模块的ui状态变化
     * @param module 模块
     * @param state 状态
     */
    private fun moduleStateChange(module: ModuleEnum, state: UiState) {
        // 正在执行的个数等于job不为null的个数
        var runningCount = 0
        viewModel.stateOne.value.job?.let { runningCount++ }
        viewModel.stateTwo.value.job?.let { runningCount++ }
        viewModel.stateThree.value.job?.let { runningCount++ }
        viewModel.stateFour.value.job?.let { runningCount++ }
        SerialPortManager.instance.setExecuting(runningCount)

        when (module) {
            A -> {
                with(binding.a) {
                    with(btnProgram) {
                        isEnabled = state.btnSelectorEnable
                        text = state.program?.name ?: "/"
                        alpha = if (state.btnSelectorEnable) 1f else 0.5f
                    }
                    with(btnStart) {
                        isEnabled = state.btnStartEnable
                        visibility = state.btnStartVisible
                        alpha = if (state.btnStartEnable) 1f else 0.5f
                    }
                    btnStop.visibility = state.btnStopVisible
                    tvActions.text = state.program?.actions ?: "/"
                    dashState.text = state.runtimeText
                    dashAction.text = state.currentActionText
                    dashTemp.text = state.tempText
                    dashTime.text = state.countDownText
                }
            }
            B -> {
                with(binding.b) {
                    with(btnProgram) {
                        isEnabled = state.btnSelectorEnable
                        text = state.program?.name ?: "/"
                        alpha = if (state.btnSelectorEnable) 1f else 0.5f
                    }
                    with(btnStart) {
                        isEnabled = state.btnStartEnable
                        visibility = state.btnStartVisible
                        alpha = if (state.btnStartEnable) 1f else 0.5f
                    }
                    btnStop.visibility = state.btnStopVisible
                    tvActions.text = state.program?.actions ?: "/"
                    dashState.text = state.runtimeText
                    dashAction.text = state.currentActionText
                    dashTemp.text = state.tempText
                    dashTime.text = state.countDownText
                }
            }
            C -> {
                with(binding.c) {
                    with(btnProgram) {
                        isEnabled = state.btnSelectorEnable
                        text = state.program?.name ?: "/"
                        alpha = if (state.btnSelectorEnable) 1f else 0.5f
                    }
                    with(btnStart) {
                        isEnabled = state.btnStartEnable
                        visibility = state.btnStartVisible
                        alpha = if (state.btnStartEnable) 1f else 0.5f
                    }
                    btnStop.visibility = state.btnStopVisible
                    tvActions.text = state.program?.actions ?: "/"
                    dashState.text = state.runtimeText
                    dashAction.text = state.currentActionText
                    dashTemp.text = state.tempText
                    dashTime.text = state.countDownText
                }
            }
            D -> {
                with(binding.d) {
                    with(btnProgram) {
                        isEnabled = state.btnSelectorEnable
                        text = state.program?.name ?: "/"
                        alpha = if (state.btnSelectorEnable) 1f else 0.5f
                    }
                    with(btnStart) {
                        isEnabled = state.btnStartEnable
                        visibility = state.btnStartVisible
                        alpha = if (state.btnStartEnable) 1f else 0.5f
                    }
                    btnStop.visibility = state.btnStopVisible
                    tvActions.text = state.program?.actions ?: "/"
                    dashState.text = state.runtimeText
                    dashAction.text = state.currentActionText
                    dashTemp.text = state.tempText
                    dashTime.text = state.countDownText
                }
            }
        }
    }

    /**
     * 操作按钮的ui状态变化
     * @param state 状态
     */
    private fun operationStateChange(state: OperationState) {
        with(binding.e) {
            btnPause.setBackgroundResource(if (state.pauseEnable) R.mipmap.btn_continue else R.mipmap.btn_pause)
            with(tvPause) {
                text = if (state.pauseEnable) "继 续" else "暂停摇床"
                setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (state.pauseEnable) R.color.red else R.color.dark_outline
                    )
                )
            }
            with(tvInsulating) {
                text =
                    if (state.insulatingEnable) "保温中 ${state.insulatingTemp}" else "抗体保温"
                setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (state.insulatingEnable) R.color.red else R.color.dark_outline
                    )
                )
            }
        }
    }

    /**
     * 选择程序
     * @param module [ModuleEnum] 模块
     */
    private fun showSelectProgramDialog(module: ModuleEnum) {
        val menuList = viewModel.programList.value.map { it.name }
        if (menuList.isNotEmpty()) {
            BottomMenu.show(menuList).setMessage("请选择程序")
                .setOnMenuItemClickListener { _, _, index ->
                    viewModel.onSwitchProgram(index, module)
                    false
                }
        }
    }
}