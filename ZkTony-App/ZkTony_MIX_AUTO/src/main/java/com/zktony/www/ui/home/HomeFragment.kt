package com.zktony.www.ui.home

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.*
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.R.mipmap
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.*
import com.zktony.www.R
import com.zktony.www.common.ext.total
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
                        operate.isVisible = it.job == null
                        start.isVisible = it.job == null && it.pointList.total() > 0
                        with(pause) {
                            isVisible = it.job != null
                            text = if (!it.pause) "暂停" else "继续"
                            setIconResource(if (!it.pause) mipmap.pause else mipmap.play)
                        }
                        if (it.program != null) {
                            select.text = it.program.name
                            holeNumber.text = it.pointList.total().toString()
                        } else {
                            select.text = "/"
                            holeNumber.text = "/"
                        }
                        if (it.pointList.isNotEmpty()) {
                            gradientPlate.size = it.pointList.size
                        }
                        gradientPlate.data = it.info.pairs

                        time.text = it.time.getTimeFormat()
                        currentCoagulantVolume.text = it.info.point.v1.toString()
                        currentColloidVolume.text = it.info.point.v2.toString()
                        currentSpeed.text = String.format("%.2f", it.info.speed)
                        currentLastTime.text = it.info.lastTime.getTimeFormat()
                        progress.progress = it.info.process

                        fillCoagulantImage.setBackgroundResource(if (it.fillCoagulant) mipmap.close else mipmap.right)
                        recaptureCoagulantImage.setBackgroundResource(if (it.recaptureCoagulant) mipmap.close else mipmap.left)
                        fillCoagulantText.text = if (it.fillCoagulant) "停止" else "填充(促凝剂)"
                        recaptureCoagulantText.text =
                            if (it.recaptureCoagulant) "停止" else "回吸(促凝剂)"
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