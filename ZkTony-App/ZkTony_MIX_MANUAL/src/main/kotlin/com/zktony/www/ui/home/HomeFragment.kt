package com.zktony.www.ui.home

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.base.BaseFragment
import com.zktony.common.ext.addTouchEvent
import com.zktony.common.ext.clickNoRepeat
import com.zktony.common.ext.clickScale
import com.zktony.www.R
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
                viewModel.uiState.collect {
                    binding.apply {
                    }
                }
            }
        }
    }

    private fun initView() {
        binding.apply {
            start.clickNoRepeat {
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
            with(fillCoagulant) {
                clickScale()
                clickNoRepeat {
                    viewModel.fillCoagulant()
                }
            }
            fillColloid.addTouchEvent(
                down = {
                    it.scaleX = 0.9f
                    it.scaleY = 0.9f
                    viewModel.fillColloid()
                },
                up = {
                    it.scaleX = 1f
                    it.scaleY = 1f
                    viewModel.stopFillAndRecapture()
                }
            )
            with(recaptureCoagulant) {
                clickScale()
                clickNoRepeat {
                    viewModel.recaptureCoagulant()
                }
            }
            recaptureColloid.addTouchEvent(
                down = {
                    it.scaleX = 0.9f
                    it.scaleY = 0.9f
                    viewModel.recaptureColloid()
                },
                up = {
                    it.scaleX = 1f
                    it.scaleY = 1f
                    viewModel.stopFillAndRecapture()
                }
            )
        }
    }
}