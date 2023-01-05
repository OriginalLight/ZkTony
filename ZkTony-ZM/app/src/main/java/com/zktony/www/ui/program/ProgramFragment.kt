package com.zktony.www.ui.program

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.zktony.www.R
import com.zktony.www.adapter.ProgramAdapter
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.extension.clickScale
import com.zktony.www.common.extension.isFastClick
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
        initRecyclerView()
        initButton()
    }

    /**
     * 初始化观察者
     */
    private fun initFlowCollector() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.programList.collect {
                        adapter.submitList(it)
                        if (it.isEmpty()) {
                            binding.empty.visibility = View.VISIBLE
                            binding.recyclerView.visibility = View.GONE
                        } else {
                            binding.empty.visibility = View.GONE
                            binding.recyclerView.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    /**
     * 初始化循环列表
     */
    private fun initRecyclerView() {
        binding.recyclerView.adapter = adapter
        adapter.setOnDeleteButtonClick {
            MessageDialog.show(
                "提示",
                "确定删除程序 ${it.name} 吗？",
                "确定",
                "取消"
            ).setOkButton { _, _ ->
                viewModel.delete(it)
                false
            }
        }
        adapter.setOnEditButtonClick {
            if (isFastClick()) {
                return@setOnEditButtonClick
            }
            val directions = if (it.model == 0) {
                ProgramFragmentDirections.actionNavigationProgramToNavigationZm(it.id)
            } else {
                ProgramFragmentDirections.actionNavigationProgramToNavigationRs(it.id)
            }
            findNavController().navigate(directions)
        }
    }

    /**
     * 初始化按钮
     */
    private fun initButton() {
        binding.add.run {
            clickScale()
            setOnClickListener {
                CustomDialog.build()
                    .setCustomView(object :
                        OnBindView<CustomDialog>(R.layout.layout_model_select) {
                        override fun onBind(dialog: CustomDialog, v: View) {
                            val zm = v.findViewById<MaterialButton>(R.id.zm)
                            val rs = v.findViewById<MaterialButton>(R.id.rs)
                            val cancel = v.findViewById<MaterialButton>(R.id.cancel)
                            zm.setOnClickListener {
                                if (isFastClick().not()) {
                                    dialog.dismiss()
                                    findNavController().navigate(R.id.action_navigation_program_to_navigation_zm)
                                }
                            }
                            rs.setOnClickListener {
                                if (isFastClick().not()) {
                                    dialog.dismiss()
                                    findNavController().navigate(R.id.action_navigation_program_to_navigation_rs)
                                }
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