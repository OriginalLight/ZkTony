package com.zktony.www.ui.program

import android.os.Bundle
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_TEXT_VARIATION_NORMAL
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.kongzue.dialogx.dialogs.InputDialog
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kongzue.dialogx.util.InputInfo
import com.zktony.www.R
import com.zktony.www.adapter.ProgramAdapter
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.extension.clickScale
import com.zktony.www.common.extension.showShortToast
import com.zktony.www.common.room.entity.Program
import com.zktony.www.databinding.FragmentProgramBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ProgramFragment :
    BaseFragment<ProgramViewModel, FragmentProgramBinding>(R.layout.fragment_program) {

    override val viewModel: ProgramViewModel by viewModels()

    private val programAdapter by lazy { ProgramAdapter() }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initObserver()
        initRecyclerView()
        initButton()
    }

    /**
     * 初始化观察者
     */
    private fun initObserver() {
        lifecycleScope.launch {
            viewModel.programList.collect {
                programAdapter.submitList(it)
                if(it.isEmpty()){
                    binding.empty.visibility = View.VISIBLE
                    binding.rc1.visibility = View.GONE
                } else {
                    binding.empty.visibility = View.GONE
                    binding.rc1.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        binding.rc1.adapter = programAdapter

        programAdapter.setOnEditButtonClick {
            val direction =
                ProgramFragmentDirections.actionNavigationProgramToNavigationAction(it.id)
            findNavController().navigate(direction)
        }

        programAdapter.setOnDeleteButtonClick {
            MessageDialog.show(
                "提示",
                "确定删除程序 ${it.name} 吗？",
                "确定",
                "取消"
            ).setOkButton { _, _ ->
                viewModel.deleteProgram(it)
                false
            }
        }
    }

    /**
     * 初始化按钮
     */
    private fun initButton() {
        binding.btnAdd.run {
            this.clickScale()
            this.setOnClickListener {
                InputDialog("程序添加", "请输入程序名", "确定", "取消")
                    .setCancelable(false)
                    .setInputInfo(InputInfo().setInputType(TYPE_CLASS_TEXT or TYPE_TEXT_VARIATION_NORMAL))
                    .setOkButton { _, _, inputStr ->
                        if (inputStr.trim().isEmpty()) {
                            "程序名称不能为空".showShortToast()
                            return@setOkButton false
                        }
                        viewModel.addProgram(inputStr.trim())
                        false
                    }
                    .show()
            }
        }
    }

}