package com.zktony.www.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kongzue.dialogx.dialogs.PopMenu
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnIconChangeCallBack
import com.kongzue.dialogx.util.TextInfo
import com.zktony.common.base.BaseFragment
import com.zktony.common.extension.clickScale
import com.zktony.www.R
import com.zktony.www.control.serial.SerialManager
import com.zktony.www.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>(R.layout.fragment_home) {

    override val viewModel: HomeViewModel by viewModels()

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
                launch { viewModel.aFlow.collect { onUiChange(0, it) } }
                launch { viewModel.bFlow.collect { onUiChange(1, it) } }
                launch { viewModel.cFlow.collect { onUiChange(2, it) } }
                launch { viewModel.dFlow.collect { onUiChange(3, it) } }
                launch { viewModel.buttonFlow.collect { onUiChange(it) } }
                launch {
                    SerialManager.instance.lock.collect {
                        binding.e.apply {
                            pause.isVisible = !it
                            tvPause.isVisible = !it
                            lock.isVisible = !it
                            tvLock.isVisible = !it
                        }
                    }
                }
                launch {
                    SerialManager.instance.swing.collect {
                        binding.e.apply {
                            pause.setBackgroundResource(if (it) R.mipmap.btn_pause else R.mipmap.btn_continue)
                            with(tvPause) {
                                text = if (it) "暂停摇床" else "继续摇床"
                                setTextColor(
                                    ContextCompat.getColor(
                                        context, if (it)  R.color.dark_outline else R.color.red
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initView() {
        for (i in 0..3) {
            val bind = getBind(i)
            bind.apply {
                actions.setOnClickListener { PopTip.show((it as TextView).text) }
                start.setOnClickListener { viewModel.start(i) }
                select.setOnClickListener { selectDialog(i) }
                with(stop) {
                    setOnClickListener { PopTip.show(R.mipmap.ic_stop, "长按停止") }
                    setOnLongClickListener {
                        viewModel.stop(i)
                        true
                    }
                }
            }
        }
        binding.apply {
            b.tvIcon.apply {
                text = "B"
                setTextColor(Color.parseColor("#6200EA"))
            }
            c.tvIcon.apply {
                text = "C"
                setTextColor(Color.parseColor("#00C853"))
            }
            d.tvIcon.apply {
                text = "D"
                setTextColor(Color.parseColor("#FFAB00"))
            }
            e.apply {
                with(reset) {
                    clickScale()
                    setOnClickListener { PopTip.show(R.mipmap.ic_reset, "长按复位") }
                    setOnLongClickListener {
                        viewModel.reset()
                        true
                    }
                }
                with(pause) {
                    clickScale()
                    setOnClickListener {
                        viewModel.shakeBed()
                    }
                }
                with(insulating) {
                    clickScale()
                    setOnClickListener {
                        if (viewModel.buttonFlow.value.insulating) {
                            PopTip.show("已取消保温，再次点击开启")
                        } else {
                            PopTip.show("抗体保温中，再次点击取消")
                        }
                        viewModel.antibodyWarm()
                    }
                }
                with(lock) {
                    clickScale()
                    setOnClickListener {
                        if (viewModel.buttonFlow.value.lock) {
                            PopTip.show("已解锁，10秒后上锁")
                            viewModel.unlock()
                        } else {
                            PopTip.show("已解锁")
                        }

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
    private fun onUiChange(module: Int, state: ModuleUiState) {
        // 正在执行的个数等于job不为null的个数
        val flag =
            viewModel.aFlow.value.job != null || viewModel.bFlow.value.job != null || viewModel.cFlow.value.job != null || viewModel.dFlow.value.job != null
        SerialManager.instance.run(flag)

        val bind = getBind(module)

        bind.apply {
            status.text = state.status
            action.text = state.action
            temp.text = state.temp
            time.text = state.time
            stop.visibility = state.stopVisible

            if (state.program != null) {
                if (actions.text.toString() != state.program.actions) {
                    actions.text = state.program.actions
                }
            } else {
                actions.text = "/"
            }

            with(select) {
                isEnabled = state.selectEnable
                text = state.program?.name ?: "/"
                alpha = if (state.selectEnable) 1f else 0.5f
            }
            with(start) {
                isEnabled = state.startEnable
                visibility = state.startVisible
                alpha = if (state.startEnable) 1f else 0.5f
            }
        }
    }

    /**
     * 操作按钮的ui状态变化
     * @param state [UiState] 状态
     */
    private fun onUiChange(state: UiState) {
        binding.e.apply {
            with(tvInsulating) {
                text = if (state.insulating) "保温中 ${state.temp}" else "抗体保温"
                setTextColor(
                    ContextCompat.getColor(
                        context, if (state.insulating) R.color.red else R.color.dark_outline
                    )
                )
            }
            with(tvLock) {
                text = if (state.lock) "已上锁" else "已开锁"
                lock.setBackgroundResource(if (state.lock) R.mipmap.btn_lock else R.mipmap.btn_unlock)
                setTextColor(
                    ContextCompat.getColor(
                        context, if (state.lock) R.color.dark_outline else R.color.green
                    )
                )
            }
        }
    }

    /**
     * 选择程序
     * @param module [Int] 模块
     */
    private fun selectDialog(module: Int) {
        val menuList = viewModel.programFlow.value.map { it.name }
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

}