package com.zktony.www.ui.program

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.kongzue.dialogx.dialogs.PopMenu
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnIconChangeCallBack
import com.kongzue.dialogx.util.TextInfo
import com.zktony.common.base.BaseFragment
import com.zktony.common.extension.afterTextChange
import com.zktony.common.extension.clickScale
import com.zktony.www.R
import com.zktony.www.adapter.ActionAdapter
import com.zktony.www.data.local.room.entity.ActionEnum
import com.zktony.www.data.local.room.entity.getActionEnum
import com.zktony.www.databinding.FragmentActionBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ActionFragment :
    BaseFragment<ActionViewModel, FragmentActionBinding>(R.layout.fragment_action) {

    override val viewModel: ActionViewModel by viewModels()

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
                                inputCount.visibility = View.VISIBLE
                                inputTime.hint = resources.getString(R.string.hint_time_min)
                            }
                        } else {
                            binding.apply {
                                inputCount.visibility = View.GONE
                                inputTime.hint = resources.getString(R.string.hint_time_hour)
                            }
                        }
                    }
                }
            }
        }
    }


    private fun initView() {
        arguments?.let {
            ActionFragmentArgs.fromBundle(it).id.run {
                if (this != "None") {
                    viewModel.init(this)
                }
            }
        }
        adapter.setOnDeleteButtonClick {
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
                setOnClickListener {
                    findNavController().navigateUp()
                }
            }
            with(btnAdd) {
                clickScale()
                setOnClickListener {
                    viewModel.insert()
                }
            }
            with(btnAction) {
                text = getActionEnum(viewModel.action.value.mode).value
                setOnClickListener {
                    val menuList = ActionEnum.values().map { it.value }
                    PopMenu.show(binding.btnAction, menuList)
                        .setMenuTextInfo(TextInfo().apply {
                            gravity = Gravity.CENTER
                            fontSize = 16
                        })
                        .setOverlayBaseView(false)
                        .setOnIconChangeCallBack(object : OnIconChangeCallBack<PopMenu>(true) {
                            override fun getIcon(
                                dialog: PopMenu?,
                                index: Int,
                                menuText: String?
                            ): Int {
                                return when (menuText) {
                                    ActionEnum.BLOCKING_LIQUID.value -> R.mipmap.ic_blocking_liquid
                                    ActionEnum.ANTIBODY_ONE.value -> R.mipmap.ic_antibody
                                    ActionEnum.ANTIBODY_TWO.value -> R.mipmap.ic_antibody
                                    ActionEnum.WASHING.value -> R.mipmap.ic_washing
                                    else -> R.mipmap.ic_blocking_liquid
                                }
                            }
                        })
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