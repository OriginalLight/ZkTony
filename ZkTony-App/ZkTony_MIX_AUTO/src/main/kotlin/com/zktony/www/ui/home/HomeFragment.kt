package com.zktony.www.ui.home

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.R.mipmap
import com.zktony.common.base.BaseFragment
import com.zktony.common.ext.clickScale
import com.zktony.common.ext.getTimeFormat
import com.zktony.www.R
import com.zktony.www.common.extension.total
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
            with(select) {
                iconTint = null
                setOnClickListener {
                    viewModel.select(it)
                }
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
            fillCoagulant.clickScale()
            fillColloid.clickScale()
            recaptureCoagulant.clickScale()
            recaptureColloid.clickScale()
        }
    }
}