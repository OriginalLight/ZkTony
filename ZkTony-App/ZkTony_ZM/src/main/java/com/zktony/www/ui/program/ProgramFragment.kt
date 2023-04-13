package com.zktony.www.ui.program

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.zktony.core.base.BaseFragment
import com.zktony.core.dialog.messageDialog
import com.zktony.core.ext.clickNoRepeat
import com.zktony.core.ext.clickScale
import com.zktony.www.R
import com.zktony.www.common.adapter.ProgramAdapter
import com.zktony.www.databinding.FragmentProgramBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProgramFragment :
    BaseFragment<ProgramViewModel, FragmentProgramBinding>(R.layout.fragment_program) {

    override val viewModel: ProgramViewModel by viewModel()

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
        adapter.onDeleteButtonClick = {
            messageDialog(
                title = "删除程序",
                message = "是否删除${it.name}？",
                block = { viewModel.delete(it) },
            )
        }
        adapter.onEditButtonClick = {
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
                clickNoRepeat {
                    CustomDialog.build()
                        .setCustomView(object :
                            OnBindView<CustomDialog>(R.layout.layout_select) {
                            override fun onBind(dialog: CustomDialog, v: View) {
                                val zm = v.findViewById<MaterialButton>(R.id.zm)
                                val rs = v.findViewById<MaterialButton>(R.id.rs)
                                zm.clickNoRepeat(1000L) {
                                    dialog.dismiss()
                                    findNavController().navigate(R.id.action_navigation_program_to_navigation_zm)
                                }
                                rs.clickNoRepeat(1000L) {
                                    dialog.dismiss()
                                    findNavController().navigate(R.id.action_navigation_program_to_navigation_rs)
                                }
                            }
                        })
                        .setCancelable(true)
                        .setMaskColor(Color.parseColor("#4D000000"))
                        .show()
                }
            }
        }

    }

}