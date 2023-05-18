package com.zktony.www.ui.home

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.*
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.R.mipmap
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.*
import com.zktony.www.R
import com.zktony.www.core.ext.total
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
                            text =
                                if (!it.pause) getString(com.zktony.core.R.string.pause) else getString(
                                    com.zktony.core.R.string.go_on
                                )
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
                            gradientPlate.checked = it.pointList.map { p -> p.index to p.enable }
                        }
                        gradientPlate.data = it.info.pairs

                        time.text = it.time.getTimeFormat()
                        currentCoagulantVolume.text = it.info.volume.first.toString()
                        currentColloidVolume.text = it.info.volume.second.toString()
                        currentSpeed.text = String.format("%.2f", it.info.speed)
                        currentLastTime.text = it.info.lastTime.getTimeFormat()
                        progress.progress = it.info.process

                        fillCoagulantImage.setBackgroundResource(if (it.fillCoagulant) mipmap.close else mipmap.right)
                        recaptureCoagulantImage.setBackgroundResource(if (it.recaptureCoagulant) mipmap.close else mipmap.left)
                        fillCoagulantText.text =
                            if (it.fillCoagulant) getString(com.zktony.core.R.string.stop) else getString(
                                R.string.fill_coagulant
                            )
                        recaptureCoagulantText.text =
                            if (it.recaptureCoagulant) getString(com.zktony.core.R.string.stop) else getString(
                                R.string.back_coagulant
                            )
                    }
                }
            }
        }
    }

    private fun initView() {
        binding.apply {
            holeNumber.clickNoRepeat {
                PopTip.show("${getString(com.zktony.core.R.string.total)}: ${holeNumber.text}")
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
                    PopTip.show(getString(com.zktony.core.R.string.press_and_hold_to_reset))
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