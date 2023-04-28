package com.zktony.www.ui.program

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.*
import com.zktony.www.R
import com.zktony.www.common.adapter.ActionAdapter
import com.zktony.www.databinding.FragmentActionBinding
import com.zktony.www.room.entity.ActionEnum
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
                viewModel.uiState.collect {
                    binding.apply {
                        adapter.submitList(it.actionList)
                        recycleView.isVisible = it.actionList.isNotEmpty()
                        empty.isVisible = it.actionList.isEmpty()
                        btnBox.text = Box.values().first { box -> box.index == it.box }.value
                        btnAction.text =
                            ActionEnum.values().first { action -> action.index == it.action }.value
                        if (it.order == 0) {
                            order.setEqualText("")
                        } else {
                            order.setEqualText(it.order.toString())
                        }
                        if (it.time == 0f) {
                            time.setEqualText("")
                        } else {
                            time.setEqualText(it.time.toString().removeZero())
                        }
                        if (it.temp == 0f) {
                            temperature.setEqualText("")
                        } else {
                            temperature.setEqualText(it.temp.toString().removeZero())
                        }
                        if (it.volume == 0) {
                            liquidVolume.setEqualText("")
                        } else {
                            liquidVolume.setEqualText(it.volume.toString())
                        }
                        if (it.count == 0) {
                            count.setEqualText("")
                        } else {
                            count.setEqualText(it.count.toString())
                        }
                        btnAdd.isEnabled = viewModel.validate()
                        if (it.action == ActionEnum.WASHING.index) {
                            llCount.visibility = View.VISIBLE
                            dividerCount.visibility = View.VISIBLE
                            tvTime.text = resources.getString(R.string.time_min)
                        } else {
                            llCount.visibility = View.GONE
                            dividerCount.visibility = View.GONE
                            tvTime.text = resources.getString(R.string.time_hour)
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
                viewModel.init(id)
            }
        }
        adapter.onDeleteButtonClick = {
            viewModel.delete(it)
        }
        binding.apply {
            recycleView.adapter = adapter

            order.afterTextChange {
                viewModel.editOrder(it.toIntOrNull() ?: 0)
            }
            time.afterTextChange {
                viewModel.editTime(it.toFloatOrNull() ?: 0f)
            }
            temperature.afterTextChange {
                viewModel.editTemp(it.toFloatOrNull() ?: 0f)
            }
            liquidVolume.afterTextChange {
                viewModel.editVolume(it.toIntOrNull() ?: 0)
            }
            count.afterTextChange {
                viewModel.editCount(it.toIntOrNull() ?: 0)
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
            btnAction.clickNoRepeat {
                val menuList = ActionEnum.values().map { it.value }
                spannerDialog(
                    view = binding.btnAction,
                    font = 16,
                    menu = menuList,
                    block = { _, index ->
                        viewModel.switchAction(index)
                    }
                )
            }
            btnBox.clickNoRepeat {
                val menuList = Box.values().map { it.value }
                spannerDialog(
                    view = binding.btnBox,
                    font = 16,
                    menu = menuList,
                    block = { _, index ->
                        viewModel.switchBox(index)
                    }
                )
            }
        }
    }
}