package com.zktony.www.ui.home

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.R.mipmap
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.addTouchEvent
import com.zktony.core.ext.afterTextChange
import com.zktony.core.ext.clickNoRepeat
import com.zktony.core.ext.clickScale
import com.zktony.core.ext.getTimeFormat
import com.zktony.core.ext.setEqualText
import com.zktony.core.ext.spannerDialog
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
                            if (it.coagulant > 0) {
                                coagulantEdit.setEqualText(it.coagulant.toString())
                            }
                            if (it.colloid > 0) {
                                colloidEdit.setEqualText(it.colloid.toString())
                            }
                            if (it.previousCoagulant > 0) {
                                previousCoagulantEdit.setEqualText(it.previousCoagulant.toString())
                            }
                            if (it.previousColloid > 0) {
                                previousColloidEdit.setEqualText(it.previousColloid.toString())
                            }

                            start.isEnabled =
                                it.coagulant > 0 && it.colloid > 0 && it.job == null && !it.start
                            previous.isEnabled =
                                it.previousCoagulant > 0 && it.previousColloid > 0 && it.job == null

                            previous.isVisible = it.previous
                            start.isVisible = !it.previous
                            normalContainer.isVisible = !it.previous
                            previousContainer.isVisible = it.previous

                            operate.isVisible = it.job == null
                            coagulantEdit.isEnabled = it.job == null
                            colloidEdit.isEnabled = it.job == null
                            coagulantHistory.isEnabled = it.job == null
                            colloidHistory.isEnabled = it.job == null
                            previousColloidEdit.isEnabled = it.job == null
                            previousCoagulantEdit.isEnabled = it.job == null
                            previousCoagulantHistory.isEnabled = it.job == null
                            previousColloidHistory.isEnabled = it.job == null

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
            previous.clickNoRepeat {
                viewModel.previous()
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

            previousColloidEdit.afterTextChange {
                viewModel.previousColloidEdit(it)
            }

            previousCoagulantEdit.afterTextChange {
                viewModel.previousCoagulantEdit(it)
            }

            with(coagulantHistory) {
                clickScale()
                clickNoRepeat {
                    val menu = viewModel.uiState.value.coagulantHistory.toList().reversed()
                    if (menu.isEmpty()) {
                        return@clickNoRepeat
                    }
                    spannerDialog(
                        view = binding.coagulantEdit,
                        font = 30,
                        menu = menu,
                        block = { str, _ -> viewModel.selectCoagulant(str) }
                    )
                }
            }

            with(previousCoagulantHistory) {
                clickScale()
                clickNoRepeat {
                    val menu = viewModel.uiState.value.previousCoagulantHistory.toList().reversed()
                    if (menu.isEmpty()) {
                        return@clickNoRepeat
                    }
                    spannerDialog(
                        view = binding.previousCoagulantEdit,
                        font = 30,
                        menu = menu,
                        block = { str, _ -> viewModel.selectPreviousCoagulant(str) }
                    )
                }
            }

            with(colloidHistory) {
                clickScale()
                clickNoRepeat {
                    val menu = viewModel.uiState.value.colloidHistory.toList().reversed()
                    if (menu.isEmpty()) {
                        return@clickNoRepeat
                    }
                    spannerDialog(
                        view = binding.colloidEdit,
                        font = 30,
                        menu = menu,
                        block = { str, _ -> viewModel.selectColloid(str) }
                    )
                }
            }

            with(previousColloidHistory) {
                clickScale()
                clickNoRepeat {
                    val menu = viewModel.uiState.value.previousColloidHistory.toList().reversed()
                    if (menu.isEmpty()) {
                        return@clickNoRepeat
                    }
                    spannerDialog(
                        view = binding.previousColloidEdit,
                        font = 30,
                        menu = menu,
                        block = { str, _ -> viewModel.selectPreviousColloid(str) }
                    )
                }
            }
        }
    }
}