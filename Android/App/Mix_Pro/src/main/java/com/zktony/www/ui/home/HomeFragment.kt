package com.zktony.www.ui.home

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnBindView
import com.zktony.core.R.mipmap
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.*
import com.zktony.www.R
import com.zktony.www.core.ext.serialPort
import com.zktony.www.databinding.FragmentHomeBinding
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>(R.layout.fragment_home) {

    override val viewModel: HomeViewModel by viewModel()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()

    }

    @SuppressLint("SetTextI18n")
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect {
                        binding.apply {
                            start.isEnabled = it.job == null && !it.lock
                            operate.isVisible = it.job == null
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
                            if (it.cache.isNotEmpty()) {
                                tvGlueColloid.text = "制胶-胶体：${it.cache[0]} μL"
                                tvGlueCoagulant.text = "制胶-促凝剂：${it.cache[1]} μL"
                                tvPreColloid.text = "预排-胶体：${it.cache[2]} μL"
                                tvPreCoagulant.text = "预排-促凝剂：${it.cache[3]} μL"
                                tvYAxis.text = "托盘位置：${it.cache[4]}"
                                tvZAxis.text = "针头位置：${it.cache[5]}"
                            }
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
                                    viewModel.wash(0)
                                    job = scope.launch {
                                        btnStart.isEnabled = false
                                        var i = time
                                        while (i > 0) {
                                            input.setText(i.toString())
                                            delay(1000L)
                                            i--
                                        }
                                        viewModel.wash(1)
                                        input.setText(time.toString())
                                        btnStart.isEnabled = true
                                    }
                                }
                                btnStop.setOnClickListener {
                                    viewModel.wash(1)
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

            cardView.clickNoRepeat {
                CustomDialog.build()
                    .setCustomView(object : OnBindView<CustomDialog>(R.layout.layout_volume) {
                        override fun onBind(dialog: CustomDialog, v: View) {
                            val glueColloid = v.findViewById<EditText>(R.id.glue_colloid)
                            val glueCoagulant = v.findViewById<EditText>(R.id.glue_coagulant)
                            val preCoagulant = v.findViewById<EditText>(R.id.pre_coagulant)
                            val preColloid = v.findViewById<EditText>(R.id.pre_colloid)
                            val yAxis = v.findViewById<EditText>(R.id.y_axis)
                            val zAxis = v.findViewById<EditText>(R.id.z_axis)
                            val btnOk = v.findViewById<MaterialButton>(R.id.ok)
                            val btnMove = v.findViewById<MaterialButton>(R.id.move)
                            val btnCancel = v.findViewById<MaterialButton>(R.id.cancel)

                            val cache = viewModel.uiState.value.cache

                            glueColloid.setText(cache[0].format())
                            glueCoagulant.setText(cache[1].format())
                            preColloid.setText(cache[2].format())
                            preCoagulant.setText(cache[3].format())
                            yAxis.setText(cache[4].format())
                            zAxis.setText(cache[5].format())

                            val scope = CoroutineScope(Dispatchers.Main)
                            btnOk.setOnClickListener {
                                scope.launch {
                                    val v1 = glueColloid.text.toString().toFloatOrNull() ?: 0f
                                    val v2 = glueCoagulant.text.toString().toFloatOrNull() ?: 0f
                                    val v3 = preColloid.text.toString().toFloatOrNull() ?: 0f
                                    val v4 = preCoagulant.text.toString().toFloatOrNull() ?: 0f
                                    val a1 = yAxis.text.toString().toFloatOrNull() ?: 0f
                                    val a2 = zAxis.text.toString().toFloatOrNull() ?: 0f
                                    if (a1 > 80f || a2 > 38f) {
                                        PopTip.show("超出范围-> Y轴最大80，Z轴最大30")
                                        return@launch
                                    } else {
                                        viewModel.setCache(listOf(v1, v2, v3, v4, a1, a2))
                                        dialog.dismiss()
                                    }
                                }
                            }
                            btnMove.setOnClickListener {
                                scope.launch {
                                    btnMove.isEnabled = false
                                    val y = yAxis.text.toString().toFloatOrNull() ?: 0f
                                    val z = zAxis.text.toString().toFloatOrNull() ?: 0f
                                    viewModel.moveTo(y, z)
                                    delay(200L)
                                    while (serialPort.lock.value) {
                                        delay(10L)
                                    }
                                    btnMove.isEnabled = true
                                }
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
    }
}