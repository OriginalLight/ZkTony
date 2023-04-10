package com.zktony.www.ui.home

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.R.mipmap
import com.zktony.common.base.BaseFragment
import com.zktony.common.ext.addTouchEvent
import com.zktony.common.ext.clickNoRepeat
import com.zktony.common.ext.clickScale
import com.zktony.common.ext.getTimeFormat
import com.zktony.www.R
import com.zktony.www.common.ext.total
import com.zktony.www.common.ext.washDialog
import com.zktony.www.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>(R.layout.fragment_home) {

    override val viewModel: HomeViewModel by viewModel()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()

    }

    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect {
                        binding.apply {
                            operate.isVisible = it.job == null
                            start.isVisible =
                                it.job == null && it.holeList.total() > 0
                            with(pause) {
                                isVisible = it.job != null
                                text = if (!it.pause) "暂停" else "继续"
                                setIconResource(if (!it.pause) mipmap.pause else mipmap.play)
                            }
                            if (it.program != null) {
                                select.text = it.program.name
                                holeNumber.text = it.holeList.total().toString()
                            } else {
                                select.text = "/"
                                holeNumber.text = "/"
                            }
                            time.text = it.time.getTimeFormat()
                            with(dynamicPlate) {
                                x = it.info.plateSize.second
                                y = it.info.plateSize.first
                                data = it.info.holeList
                                color = it.info.color
                            }
                            currentPlate.text = it.info.plate
                            currentLiquid.text = it.info.liquid
                            currentSpeed.text = String.format("%.2f", it.info.speed)
                            currentLastTime.text = it.info.lastTime.getTimeFormat()
                            progress.progress = it.info.process
                        }
                    }
                }
            }
        }
    }

    private fun initView() {
        binding.apply {
            holeNumber.clickNoRepeat {
                PopTip.show("加液总数: ${holeNumber.text}")
            }
            with(select) {
                iconTint = null
                clickNoRepeat {
                    viewModel.select(it)
                }
            }
            start.clickNoRepeat {
                viewModel.start()
            }
            stop.clickNoRepeat {
                viewModel.stop()
            }
            pause.clickNoRepeat {
                viewModel.pause()
            }
            with(reset) {
                clickScale()
                clickNoRepeat {
                    PopTip.show("长按复位")
                }
                setOnLongClickListener {
                    viewModel.reset()
                    true
                }
            }
            with(waste) {
                clickScale()
                clickNoRepeat {
                    viewModel.waste()
                }
            }
            with(wash) {
                clickScale()
                clickNoRepeat {
                    washDialog(
                        {
                            viewModel.wash(time = it, type = 0)
                        },
                        {
                            viewModel.wash(type = 1)
                        }
                    )
                }
            }
            fill.addTouchEvent({
                it.scaleX = 0.8f
                it.scaleY = 0.8f
                viewModel.fill(0)
            }, {
                it.scaleX = 1f
                it.scaleY = 1f
                viewModel.fill(1)
            })
            suckBack.addTouchEvent({
                it.scaleX = 0.8f
                it.scaleY = 0.8f
                viewModel.suckBack(0)
            }, {
                it.scaleX = 1f
                it.scaleY = 1f
                viewModel.suckBack(1)
            })
        }
    }
}