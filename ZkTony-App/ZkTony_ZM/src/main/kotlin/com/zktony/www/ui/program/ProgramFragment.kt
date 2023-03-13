package com.zktony.www.ui.program

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.zktony.common.base.BaseFragment
import com.zktony.common.dialog.deleteDialog
import com.zktony.common.ext.clickNoRepeat
import com.zktony.common.ext.clickScale
import com.zktony.www.R
import com.zktony.www.adapter.ProgramAdapter
import com.zktony.www.databinding.FragmentProgramBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProgramFragment :
    BaseFragment<ProgramViewModel, FragmentProgramBinding>(R.layout.fragment_program) {

    override val viewModel: ProgramViewModel by viewModels()

    private val adapter by lazy { ProgramAdapter() }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()
    }

    /**
     * 初始化观察者
     */
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.programList.collect {
                    adapter.submitList(it)
                    binding.apply {
                        empty.isVisible = it.isEmpty()
                        recyclerView.isVisible = it.isNotEmpty()
                    }
                }
            }
        }
    }

    /**
     * 初始化循环列表
     */
    private fun initView() {
        adapter.setOnDeleteButtonClick {
            deleteDialog(
                name = it.name,
                block = { viewModel.delete(it) })
        }
        adapter.setOnEditButtonClick {
            findNavController().navigate(
                if (it.model == 0) {
                    R.id.action_navigation_program_to_navigation_zm
                } else {
                    R.id.action_navigation_program_to_navigation_rs
                },
                Bundle().apply { putString("id", it.id) }
            )
        }
        binding.apply {
            recyclerView.adapter = adapter
            with(add) {
                clickScale()
                setOnClickListener {
                    CustomDialog.build()
                        .setCustomView(object :
                            OnBindView<CustomDialog>(R.layout.layout_model_select) {
                            override fun onBind(dialog: CustomDialog, v: View) {
                                val zm = v.findViewById<MaterialButton>(R.id.zm)
                                val rs = v.findViewById<MaterialButton>(R.id.rs)
                                val cancel = v.findViewById<MaterialButton>(R.id.cancel)
                                zm.clickNoRepeat {
                                    dialog.dismiss()
                                    findNavController().navigate(R.id.action_navigation_program_to_navigation_zm)
                                }
                                rs.clickNoRepeat {
                                    dialog.dismiss()
                                    findNavController().navigate(R.id.action_navigation_program_to_navigation_rs)
                                }
                                cancel.setOnClickListener { dialog.dismiss() }
                            }
                        })
                        .setCancelable(false)
                        .setMaskColor(Color.parseColor("#4D000000"))
                        .show()
                }
            }
        }

    }

}