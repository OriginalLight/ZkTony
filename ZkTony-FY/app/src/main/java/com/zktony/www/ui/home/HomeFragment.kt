package com.zktony.www.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kongzue.dialogx.dialogs.PopMenu
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnIconChangeCallBack
import com.kongzue.dialogx.util.TextInfo
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.extension.clickScale
import com.zktony.www.databinding.FragmentHomeBinding
import com.zktony.www.serial.SerialManager
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
                launch { viewModel.aFlow.collect { moduleUiChange(A, it) } }
                launch { viewModel.bFlow.collect { moduleUiChange(B, it) } }
                launch { viewModel.cFlow.collect { moduleUiChange(C, it) } }
                launch { viewModel.dFlow.collect { moduleUiChange(D, it) } }
                launch { viewModel.uiFlow.collect { uiChange(it) } }
            }
        }
    }

    /**
     * 初始化文本框
     */
    @SuppressLint("SetTextI18n")
    private fun initTextView() {
        binding.a.actions.setOnClickListener { PopTip.show((it as TextView).text) }
        binding.b.actions.setOnClickListener { PopTip.show((it as TextView).text) }
        binding.c.actions.setOnClickListener { PopTip.show((it as TextView).text) }
        binding.d.actions.setOnClickListener { PopTip.show((it as TextView).text) }
    }

    /**
     * 初始化按钮
     */
    private fun initButton() {
        binding.run {
            a.run {
                start.setOnClickListener { this@HomeFragment.viewModel.start(A) }
                stop.run {
                    setOnClickListener { PopTip.show(R.mipmap.ic_stop, "长按停止") }
                    setOnLongClickListener {
                        this@HomeFragment.viewModel.stop(A)
                        true
                    }
                }
                select.setOnClickListener { showProgramDialog(A) }
            }
            b.run {
                start.setOnClickListener { this@HomeFragment.viewModel.start(B) }
                stop.run {
                    setOnClickListener { PopTip.show(R.mipmap.ic_stop, "长按停止") }
                    setOnLongClickListener {
                        this@HomeFragment.viewModel.stop(B)
                        true
                    }
                }
                select.setOnClickListener { showProgramDialog(B) }
            }
            c.run {
                start.setOnClickListener { this@HomeFragment.viewModel.start(C) }
                stop.run {
                    setOnClickListener {
                        PopTip.show(R.mipmap.ic_stop, "长按停止")
                    }
                    setOnLongClickListener {
                        this@HomeFragment.viewModel.stop(C)
                        true
                    }
                }
                select.setOnClickListener { showProgramDialog(C) }
            }
            d.run {
                start.setOnClickListener { this@HomeFragment.viewModel.start(D) }
                stop.run {
                    setOnClickListener {
                        PopTip.show(R.mipmap.ic_stop, "长按停止")
                    }
                    setOnLongClickListener {
                        this@HomeFragment.viewModel.stop(D)
                        true
                    }
                }
                select.setOnClickListener { showProgramDialog(D) }
            }
            e.run {
                reset.run {
                    clickScale()
                    setOnClickListener { PopTip.show(R.mipmap.ic_reset, "长按复位") }
                    setOnLongClickListener {
                        this@HomeFragment.viewModel.reset()
                        true
                    }
                }
                pause.run {
                    clickScale()
                    setOnClickListener {
                        if (this@HomeFragment.viewModel.uiFlow.value.pause) {
                            PopTip.show("已继续摇床，再次点击暂停")
                        } else {
                            PopTip.show("已暂停摇床，再次点击继续")
                        }
                        this@HomeFragment.viewModel.shakeBed()
                    }
                }
                insulating.run {
                    clickScale()
                    setOnClickListener {
                        if (this@HomeFragment.viewModel.uiFlow.value.insulating) {
                            PopTip.show("已取消保温，再次点击开启")
                        } else {
                            PopTip.show("抗体保温中，再次点击取消")
                        }
                        this@HomeFragment.viewModel.antibodyWarm()
                    }
                }
            }
        }
    }

    /**
     * 各个模块的ui状态变化
     * @param module 模块
     * @param state 状态
     */
    private fun moduleUiChange(module: ModuleEnum, state: ModuleUiState) {
        // 正在执行的个数等于job不为null的个数
        var runningCount = 0
        viewModel.aFlow.value.job?.let { runningCount++ }
        viewModel.bFlow.value.job?.let { runningCount++ }
        viewModel.cFlow.value.job?.let { runningCount++ }
        viewModel.dFlow.value.job?.let { runningCount++ }
        SerialManager.instance.executing = runningCount

        when (module) {
            A -> {
                binding.a.run {
                    select.run {
                        isEnabled = state.selectEnable
                        text = state.program?.name ?: "/"
                        alpha = if (state.selectEnable) 1f else 0.5f
                    }
                    start.run {
                        isEnabled = state.startEnable
                        visibility = state.startVisible
                        alpha = if (state.startEnable) 1f else 0.5f
                    }
                    stop.visibility = state.stopVisible
                    actions.text = state.program?.actions ?: "/"
                    status.text = state.status
                    action.text = state.action
                    temp.text = state.temp
                    time.text = state.time
                }
            }
            B -> {
                binding.b.run {
                    select.run {
                        isEnabled = state.selectEnable
                        text = state.program?.name ?: "/"
                        alpha = if (state.selectEnable) 1f else 0.5f
                    }
                    start.run {
                        isEnabled = state.startEnable
                        visibility = state.startVisible
                        alpha = if (state.startEnable) 1f else 0.5f
                    }
                    stop.visibility = state.stopVisible
                    actions.text = state.program?.actions ?: "/"
                    status.text = state.status
                    action.text = state.action
                    temp.text = state.temp
                    time.text = state.time
                }
            }
            C -> {
                binding.c.run {
                    select.run {
                        isEnabled = state.selectEnable
                        text = state.program?.name ?: "/"
                        alpha = if (state.selectEnable) 1f else 0.5f
                    }
                    start.run {
                        isEnabled = state.startEnable
                        visibility = state.startVisible
                        alpha = if (state.startEnable) 1f else 0.5f
                    }
                    stop.visibility = state.stopVisible
                    actions.text = state.program?.actions ?: "/"
                    status.text = state.status
                    action.text = state.action
                    temp.text = state.temp
                    time.text = state.time
                }
            }
            D -> {
                binding.d.run {
                    select.run {
                        isEnabled = state.selectEnable
                        text = state.program?.name ?: "/"
                        alpha = if (state.selectEnable) 1f else 0.5f
                    }
                    start.run {
                        isEnabled = state.startEnable
                        visibility = state.startVisible
                        alpha = if (state.startEnable) 1f else 0.5f
                    }
                    stop.visibility = state.stopVisible
                    actions.text = state.program?.actions ?: "/"
                    status.text = state.status
                    action.text = state.action
                    temp.text = state.temp
                    time.text = state.time
                }
            }
        }
    }

    /**
     * 操作按钮的ui状态变化
     * @param state [UiState] 状态
     */
    private fun uiChange(state: UiState) {
        binding.e.run {
            pause.setBackgroundResource(if (state.pause) R.mipmap.btn_continue else R.mipmap.btn_pause)
            tvPause.run {
                text = if (state.pause) "继 续" else "暂停摇床"
                setTextColor(
                    ContextCompat.getColor(
                        context, if (state.pause) R.color.red else R.color.dark_outline
                    )
                )
            }
            tvInsulating.run {
                text = if (state.insulating) "保温中 ${state.temp}" else "抗体保温"
                setTextColor(
                    ContextCompat.getColor(
                        context, if (state.insulating) R.color.red else R.color.dark_outline
                    )
                )
            }
        }
    }


    /**
     * 选择程序
     * @param module [ModuleEnum] 模块
     */
    private fun showProgramDialog(module: ModuleEnum) {
        val menuList = viewModel.proFlow.value.map { it.name }
        if (menuList.isEmpty()) {
            PopTip.show("请先添加程序")
            return
        }
        PopMenu.show(menuList).setMenuTextInfo(TextInfo().apply {
            gravity = Gravity.CENTER
            fontSize = 16
        }).setOnIconChangeCallBack(object : OnIconChangeCallBack<PopMenu>(true) {
            override fun getIcon(dialog: PopMenu?, index: Int, menuText: String?): Int {
                return R.mipmap.ic_program
            }
        }).setOnMenuItemClickListener { _, _, index ->
            viewModel.selectProgram(index, module)
            false
        }.width = 300
    }

}