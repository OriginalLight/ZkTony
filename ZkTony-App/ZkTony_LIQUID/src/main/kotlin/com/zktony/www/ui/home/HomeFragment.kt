package com.zktony.www.ui.home

import android.os.Bundle
import android.util.Log
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.base.BaseFragment
import com.zktony.common.extension.addTouchEvent
import com.zktony.common.extension.clickScale
import com.zktony.common.extension.getTimeFormat
import com.zktony.www.R
import com.zktony.www.common.extension.total
import com.zktony.www.common.extension.washDialog
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

    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect {
                        Log.d("HomeFragment", it.info.toString())
                        binding.apply {
                            operate.isVisible = it.job == null
                            start.isEnabled =
                                it.job == null && it.holeList.total() > 0
                            with(pause) {
                                isEnabled = it.job != null
                                text = if (!it.pause) "暂停" else "继续"
                                alpha = if (it.job != null) 1f else 0.3f
                            }
                            if (it.work != null) {
                                select.text = it.work.name
                                holeNumber.text = it.holeList.total().toString()
                            } else {
                                select.text = "/"
                                holeNumber.text = "/"
                            }
                            time.text = it.time.getTimeFormat()
                            dynamicPlate.setXY(
                                it.info.plateSize.second,
                                it.info.plateSize.first,
                            )
                            dynamicPlate.setData(it.info.holeList)
                            dynamicPlate.setColor(it.info.color)
                            currentPlate.text = it.info.plate
                            currentLiquid.text = it.info.liquid
                            currentSpeed.text = String.format("%.2f", it.info.speed)
                            currentLastTime.text = it.info.lastTime.getTimeFormat()
                        }
                    }
                }
            }
        }
    }

    private fun initView() {
        binding.apply {
            holeNumber.setOnClickListener {
                PopTip.show("加液总数: ${holeNumber.text}")
            }
            select.setOnClickListener {
                viewModel.selectWork(it)
            }
            start.setOnClickListener {
                viewModel.start()
            }
            stop.setOnClickListener {
                viewModel.stop()
            }
            pause.setOnClickListener {
                viewModel.pause()
            }
            with(reset) {
                clickScale()
                setOnClickListener {
                    PopTip.show("长按复位")
                }
                setOnLongClickListener {
                    viewModel.reset()
                    true
                }
            }
            with(wash) {
                clickScale()
                setOnClickListener {
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