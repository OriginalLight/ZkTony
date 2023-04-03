package com.zktony.www.ui.home

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.base.BaseFragment
import com.zktony.common.dialog.spannerDialog
import com.zktony.common.ext.*
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
                        if (it.coagulant > 0) {
                            coagulantEdit.setEqualText(it.coagulant.toString())
                        }
                        if (it.colloid > 0) {
                            colloidEdit.setEqualText(it.colloid.toString())
                        }
                        start.isEnabled = it.coagulant > 0 && it.colloid > 0
                        start.isVisible = it.job == null
                        operate.isVisible = it.job == null
                        coagulantEdit.isEnabled = it.job == null
                        colloidEdit.isEnabled = it.job == null
                        coagulantHistory.isClickable = it.job == null
                        colloidHistory.isClickable = it.job == null
                        timeText.text = it.time.getTimeFormat()
                        fillCoagulantImage.setBackgroundResource(if (it.fillCoagulant) com.zktony.common.R.mipmap.close else com.zktony.common.R.mipmap.right)
                        recaptureCoagulantImage.setBackgroundResource(if (it.recaptureCoagulant) com.zktony.common.R.mipmap.close else com.zktony.common.R.mipmap.left)
                        fillCoagulantText.text = if (it.fillCoagulant) "停止" else "填充(促凝剂)"
                        recaptureCoagulantText.text = if (it.recaptureCoagulant) "停止" else "回吸(促凝剂)"
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

            coagulantEdit.afterTextChange {
                viewModel.coagulantEdit(it)
            }

            colloidEdit.afterTextChange {
                viewModel.colloidEdit(it)
            }

            with(coagulantHistory) {
                clickScale()
                clickNoRepeat {
                    val menu = viewModel.uiState.value.coagulantHistory.toList().reversed()
                    if (menu.isEmpty()) {
                        PopTip.show("历史记录为空")
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
            with(colloidHistory) {
                clickScale()
                clickNoRepeat {
                    val menu = viewModel.uiState.value.colloidHistory.toList().reversed()
                    if (menu.isEmpty()) {
                        PopTip.show("历史记录为空")
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
        }
    }
}