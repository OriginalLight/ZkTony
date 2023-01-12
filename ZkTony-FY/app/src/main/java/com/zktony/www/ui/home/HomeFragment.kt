package com.zktony.www.ui.home

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>(R.layout.fragment_home) {

    override val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var appViewModel: AppViewModel

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initTextView(0)
        initTextView(1)
        initTextView(2)
        initTextView(3)
        initButton(0)
        initButton(1)
        initButton(2)
        initButton(3)
        initCommonButton()
    }

    /**
     * 初始化观察者
     */
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.aFlow.collect { moduleUiChange(0, it) } }
                launch { viewModel.bFlow.collect { moduleUiChange(1, it) } }
                launch { viewModel.cFlow.collect { moduleUiChange(2, it) } }
                launch { viewModel.dFlow.collect { moduleUiChange(3, it) } }
                launch { viewModel.uiFlow.collect { uiChange(it) } }
                launch { SerialManager.instance.runtimeLock.collect { hideButton(it) } }
            }
        }
    }

    /**
     * 初始化文本框
     */
    @SuppressLint("SetTextI18n")
    private fun initTextView(module: Int) {
        val bind = getBind(module)
        bind.actions.setOnClickListener { PopTip.show((it as TextView).text) }
    }

    /**
     * 初始化按钮
     */
    private fun initButton(module: Int) {
        val bind = getBind(module)
        bind.run {
            start.setOnClickListener { this@HomeFragment.viewModel.start(module) }
            stop.run {
                setOnClickListener { PopTip.show(R.mipmap.ic_stop, "长按停止") }
                setOnLongClickListener {
                    this@HomeFragment.viewModel.stop(module)
                    true
                }
            }
            select.setOnClickListener { showProgramDialog(module) }
        }
    }

    /**
     * 初始化共有的按钮
     */
    private fun initCommonButton() {
        binding.run {
            b.run {
                tvIcon.text = "B"
                tvIcon.setTextColor(Color.parseColor("#6200EA"))
            }
            c.run {
                tvIcon.text = "C"
                tvIcon.setTextColor(Color.parseColor("#00C853"))
            }
            d.run {
                tvIcon.text = "D"
                tvIcon.setTextColor(Color.parseColor("#FFAB00"))
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
    private fun moduleUiChange(module: Int, state: ModuleUiState) {
        // 正在执行的个数等于job不为null的个数
        var runningCount = 0
        viewModel.aFlow.value.job?.let { runningCount++ }
        viewModel.bFlow.value.job?.let { runningCount++ }
        viewModel.cFlow.value.job?.let { runningCount++ }
        viewModel.dFlow.value.job?.let { runningCount++ }
        SerialManager.instance.executing = runningCount

        val bind = getBind(module)

        bind.run {
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
            if (state.program != null) {
                if (actions.text.toString() != state.program.actions) {
                    actions.text = state.program.actions
                }
            } else {
                actions.text = "/"
            }
            status.text = state.status
            action.text = state.action
            temp.text = state.temp
            time.text = state.time
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
     * @param module [Int] 模块
     */
    private fun showProgramDialog(module: Int) {
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

    /**
     * 获取Bind
     */
    private fun getBind(module: Int) = when (module) {
        0 -> binding.a
        1 -> binding.b
        2 -> binding.c
        3 -> binding.d
        else -> binding.a
    }

    /**
     * 当机构运行时隐藏暂停按钮
     * @param lock 锁
     */
    private fun hideButton(lock: Boolean) {
        if (lock) {
            binding.e.run {
                pause.visibility = View.GONE
                tvPause.visibility = View.GONE
            }
        } else {
            binding.e.run {
                pause.visibility = View.VISIBLE
                tvPause.visibility = View.VISIBLE
            }
        }
    }
}