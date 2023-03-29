package com.zktony.www.ui.program

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.kongzue.dialogx.dialogs.PopMenu
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.util.TextInfo
import com.zktony.common.base.BaseFragment
import com.zktony.common.ext.afterTextChange
import com.zktony.common.ext.clickNoRepeat
import com.zktony.common.ext.clickScale
import com.zktony.www.R
import com.zktony.www.common.adapter.ActionAdapter
import com.zktony.www.data.local.entity.ActionEnum
import com.zktony.www.data.local.entity.getActionEnum
import com.zktony.www.databinding.FragmentActionBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ActionFragment :
    BaseFragment<ActionViewModel, FragmentActionBinding>(R.layout.fragment_action) {

    override val viewModel: ActionViewModel by viewModel()

    private val adapter by lazy { ActionAdapter() }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()
    }

    /**
     * 初始化操作
     */
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.actionList.collect {
                        adapter.submitList(it)
                        binding.apply {
                            recycleView.isVisible = it.isNotEmpty()
                            empty.isVisible = it.isEmpty()
                        }
                    }
                }
                launch {
                    viewModel.buttonEnable.collect {
                        binding.btnAdd.apply {
                            isEnabled = it
                            alpha = if (it) 1f else 0.5f
                            text =
                                if (it) resources.getString(R.string.add) else resources.getString(R.string.add_ban)
                        }
                    }
                }
                launch {
                    viewModel.action.collect {
                        if (getActionEnum(it.mode) == ActionEnum.WASHING) {
                            binding.apply {
                                llCount.visibility = View.VISIBLE
                                dividerCount.visibility = View.VISIBLE
                                tvTime.text = resources.getString(R.string.hint_time_min)
                            }
                        } else {
                            binding.apply {
                                llCount.visibility = View.GONE
                                dividerCount.visibility = View.GONE
                                tvTime.text = resources.getString(R.string.hint_time_hour)
                            }
                        }
                    }
                }
            }
        }
    }


    private fun initView() {
        arguments?.let {
            val id = it.getString("id") ?: ""
            if (id.isNotEmpty()) {
                viewModel.load(id)
            }
        }
        adapter.onDeleteButtonClick = {
            PopTip.show("已删除")
            viewModel.delete(it)
        }
        binding.apply {
            recycleView.adapter = adapter

            order.afterTextChange {
                viewModel.editAction(
                    viewModel.action.value.copy(order = it.toIntOrNull() ?: 0)
                )
            }
            time.afterTextChange {
                viewModel.editAction(
                    viewModel.action.value.copy(time = it.toFloatOrNull() ?: 0f)
                )
            }
            temperature.afterTextChange {
                viewModel.editAction(
                    viewModel.action.value.copy(temperature = it.toFloatOrNull() ?: 0f)
                )
            }
            liquidVolume.afterTextChange {
                viewModel.editAction(
                    viewModel.action.value.copy(liquidVolume = it.toFloatOrNull() ?: 0f)
                )
            }
            count.afterTextChange {
                viewModel.editAction(
                    viewModel.action.value.copy(count = it.toIntOrNull() ?: 0)
                )
            }

            with(btnBack) {
                clickScale()
                clickNoRepeat {
                    findNavController().navigateUp()
                }
            }
            with(btnAdd) {
                clickScale()
                clickNoRepeat {
                    viewModel.insert()
                }
            }
            with(btnAction) {
                text = getActionEnum(viewModel.action.value.mode).value
                clickNoRepeat {
                    val menuList = ActionEnum.values().map { it.value }
                    PopMenu.show(binding.btnAction, menuList)
                        .setMenuTextInfo(TextInfo().apply {
                            gravity = Gravity.CENTER
                            fontSize = 16
                        })
                        .setOverlayBaseView(false)
                        .setOnMenuItemClickListener { _, text, index ->
                            binding.btnAction.text = text
                            viewModel.switchAction(getActionEnum(index))
                            false
                        }
                        .setRadius(0f)
                        .alignGravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                }
            }
        }
    }
}