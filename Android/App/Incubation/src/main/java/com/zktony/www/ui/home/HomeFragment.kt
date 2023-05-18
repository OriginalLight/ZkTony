package com.zktony.www.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kongzue.dialogx.dialogs.PopMenu
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.util.TextInfo
import com.zktony.core.R.color
import com.zktony.core.R.mipmap
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.addTouchEvent
import com.zktony.core.ext.clickNoRepeat
import com.zktony.core.ext.clickScale
import com.zktony.www.R
import com.zktony.www.core.ext.collectLock
import com.zktony.www.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>(R.layout.fragment_home) {

    override val viewModel: HomeViewModel by viewModel()

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
                launch { viewModel.uiState.collect { onUiChange(it) } }
                launch {
                    collectLock {
                        binding.e.apply {
                            pause.isVisible = !it
                            tvPause.isVisible = !it
                            lock.isVisible = !it
                            tvLock.isVisible = !it
                            fill.isVisible = !it
                            tvFill.isVisible = !it
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
                actions.clickNoRepeat { PopTip.show((it as TextView).text) }
                with(startStop) {
                    clickScale()
                    clickNoRepeat {
                        val job = when (i) {
                            0 -> viewModel.aFlow.value.job
                            1 -> viewModel.bFlow.value.job
                            2 -> viewModel.cFlow.value.job
                            3 -> viewModel.dFlow.value.job
                            else -> viewModel.aFlow.value.job
                        }
                        if (job == null) {
                            viewModel.start(i)
                        } else {
                            viewModel.stop(i)
                        }
                    }
                }
                select.clickNoRepeat { selectDialog(i) }
            }
        }
        binding.apply {
            a.tvIcon.apply {
                text = "A"
                setTextColor(Color.parseColor("#D50000"))
            }
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
                fill.addTouchEvent(
                    down = {
                        it.scaleX = 0.9f
                        it.scaleY = 0.9f
                        viewModel.fill(0)
                    },
                    up = {
                        it.scaleX = 1f
                        it.scaleY = 1f
                        viewModel.fill(1)
                    }
                )
                with(reset) {
                    clickScale()
                    clickNoRepeat { PopTip.show("长按复位") }
                    setOnLongClickListener {
                        viewModel.reset()
                        true
                    }
                }
                with(pause) {
                    clickScale()
                    clickNoRepeat {
                        viewModel.shakeBed()
                    }
                }
                with(insulating) {
                    clickScale()
                    clickNoRepeat {
                        if (viewModel.uiState.value.insulating) {
                            PopTip.show("已取消保温，再次点击开启")
                        } else {
                            PopTip.show("抗体保温中，再次点击取消")
                        }
                        viewModel.antibodyWarm()
                    }
                }
                with(lock) {
                    clickScale()
                    clickNoRepeat {
                        if (viewModel.uiState.value.lock) {
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

        val bind = getBind(module)

        bind.apply {
            status.text = state.status
            action.text = state.action
            temp.text = state.temp
            time.text = state.time

            if (state.program != null) {
                if (actions.text.toString() != state.program.actions) {
                    actions.text = state.program.actions
                }
            } else {
                actions.text = "/"
            }

            with(select) {
                isEnabled = state.job == null
                text = state.program?.name ?: "/"
            }

            with(startStop) {
                if (state.job == null) {
                    val enable = state.program != null && state.program.actionCount > 0
                    isEnabled = enable
                    alpha = if (enable) 1f else 0.3f
                    background = ContextCompat.getDrawable(context, mipmap.play)
                } else {
                    isEnabled = true
                    alpha = 1f
                    background = ContextCompat.getDrawable(context, mipmap.stop)
                }
            }
        }
    }

    /**
     * 操作按钮的ui状态变化
     * @param state [HomeUiState] 状态
     */
    private fun onUiChange(state: HomeUiState) {
        binding.e.apply {
            with(tvInsulating) {
                text = if (state.insulating) "保温中 ${state.insulatingTemp}" else "抗体保温"
                setTextColor(
                    ContextCompat.getColor(
                        context, if (state.insulating) color.red else color.dark_outline
                    )
                )
            }
            with(tvLock) {
                text = if (state.lock) "已上锁" else "已开锁"
                lock.setBackgroundResource(if (state.lock) mipmap.lock else mipmap.unlock)
                setTextColor(
                    ContextCompat.getColor(
                        context, if (state.lock) color.dark_outline else color.green
                    )
                )
            }

            pause.setBackgroundResource(if (state.shakeBed) mipmap.pause else mipmap.play)
            with(tvPause) {
                text =
                    if (state.shakeBed) getString(R.string.pause) else getString(com.zktony.core.R.string.go_on)
                setTextColor(
                    ContextCompat.getColor(
                        context, if (state.shakeBed) color.dark_outline else color.red
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
        val menuList = viewModel.uiState.value.programList.map { it.name }
        if (menuList.isEmpty()) {
            PopTip.show("请先添加程序")
            return
        }
        PopMenu.show(menuList).setMenuTextInfo(TextInfo().apply {
            gravity = Gravity.CENTER
            fontSize = 16
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