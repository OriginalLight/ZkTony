package com.zktony.www.ui.home

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.R.mipmap
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.*
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
                launch {
                    viewModel.uiState.collect {
                        binding.apply {
                            if (it.coagulant > 0f) {
                                coagulantEdit.setEqualText(it.coagulant.format())
                            } else {
                                coagulantEdit.setEqualText("")
                            }
                            if (it.colloid > 0f) {
                                colloidEdit.setEqualText(it.colloid.format())
                            } else {
                                colloidEdit.setEqualText("")
                            }

                            start.isEnabled =
                                it.coagulant > 0f && it.colloid > 0f && it.job == null && !it.lock
                            start.text =
                                if (it.previous) getString(R.string.pre_drainage) else getString(com.zktony.core.R.string.start)

                            stop.isVisible = it.job != null
                            mode.isVisible = it.job == null

                            title.text =
                                if (it.mode) getString(R.string.mixer_mode) else getString(R.string.standard_mode)

                            operate.isVisible = it.job == null
                            coagulantEdit.isEnabled = it.job == null
                            colloidEdit.isEnabled = it.job == null
                            coagulantHistory.isEnabled = it.job == null
                            colloidHistory.isEnabled = it.job == null

                            timeText.text = it.time.getTimeFormat()
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
    }

    private fun initView() {
        binding.apply {
            start.clickNoRepeat {
                viewModel.start()
            }
            stop.clickNoRepeat {
                viewModel.stop()
            }
            with(stop) {
                clickNoRepeat {
                    PopTip.show("长按停止")
                }
                setOnLongClickListener {
                    viewModel.stop()
                    true
                }
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

            coagulantEdit.afterTextChange {
                viewModel.coagulantEdit(it)
            }

            colloidEdit.afterTextChange {
                viewModel.colloidEdit(it)
            }

            mode.clickNoRepeat {
                viewModel.mode()
            }

            with(coagulantHistory) {
                clickScale()
                clickNoRepeat {
                    val uiState = viewModel.uiState.value
                    val list = uiState.cacheList.filter { it.type == viewModel.getType() }
                    if (list.isEmpty()) {
                        return@clickNoRepeat
                    }
                    spannerDialog(
                        view = binding.coagulantEdit,
                        font = 30,
                        menu = list.first().coagulant.map { it.format() }.reversed(),
                        block = { str, _ -> viewModel.selectCoagulant(str) }
                    )
                }
            }

            with(colloidHistory) {
                clickScale()
                clickNoRepeat {
                    val list =
                        viewModel.uiState.value.cacheList.filter { it.type == viewModel.getType() }
                    if (list.isEmpty()) {
                        return@clickNoRepeat
                    }
                    spannerDialog(
                        view = binding.colloidEdit,
                        font = 30,
                        menu = list.first().colloid.map { it.format() }.reversed(),
                        block = { str, _ -> viewModel.selectColloid(str) }
                    )
                }
            }
        }
    }
}