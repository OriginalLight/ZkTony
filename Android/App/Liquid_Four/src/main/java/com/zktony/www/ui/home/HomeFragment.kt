package com.zktony.www.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.lifecycle.*
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnBindView
import com.zktony.core.R.mipmap
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.*
import com.zktony.www.R
import com.zktony.www.common.ext.total
import com.zktony.www.databinding.FragmentHomeBinding
import kotlinx.coroutines.*
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
                                it.job == null && it.pointList.total() > 0
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
                            time.text = it.time.getTimeFormat()
                            with(dynamicPlate) {
                                column = it.info.size.second
                                row = it.info.size.first
                                data = it.info.tripleList
                                color = it.info.color
                            }
                            currentPlate.text = it.info.index
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
            with(waste) {
                clickScale()
                clickNoRepeat {
                    viewModel.waste()
                }
            }
            with(wash) {
                clickScale()
                clickNoRepeat {
                    CustomDialog.build()
                        .setCustomView(object : OnBindView<CustomDialog>(R.layout.layout_wash) {
                            override fun onBind(dialog: CustomDialog, v: View) {
                                val input = v.findViewById<EditText>(R.id.input)
                                val btnStart = v.findViewById<MaterialButton>(R.id.start)
                                val btnStop = v.findViewById<MaterialButton>(R.id.stop)
                                val btnCancel = v.findViewById<MaterialButton>(R.id.cancel)
                                val time = input.text.toString().toIntOrNull() ?: 30
                                val scope = CoroutineScope(Dispatchers.Main)
                                var job: Job? = null
                                btnStart.setOnClickListener {
                                    viewModel.fill(0)
                                    job = scope.launch {
                                        btnStart.isEnabled = false
                                        var i = time
                                        while (i > 0) {
                                            input.setText(i.toString())
                                            delay(1000L)
                                            i--
                                        }
                                        viewModel.fill(1)
                                        input.setText(time.toString())
                                        btnStart.isEnabled = true
                                    }
                                }
                                btnStop.setOnClickListener {
                                    viewModel.fill(1)
                                    job?.cancel()
                                    input.setText(time.toString())
                                    btnStart.isEnabled = true
                                }
                                btnCancel.setOnClickListener {
                                    dialog.dismiss()
                                }
                            }
                        })
                        .setCancelable(false)
                        .setMaskColor(Color.parseColor("#4D000000"))
                        .show()
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