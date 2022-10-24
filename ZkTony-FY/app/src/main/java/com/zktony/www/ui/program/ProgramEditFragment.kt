package com.zktony.www.ui.program

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnIconChangeCallBack
import com.zktony.www.R
import com.zktony.www.adapter.ActionAdapter
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.extension.afterTextChange
import com.zktony.www.common.extension.clickScale
import com.zktony.www.data.entity.Action
import com.zktony.www.databinding.FragmentProgramEditBinding
import com.zktony.www.model.enum.ActionEnum
import com.zktony.www.model.enum.getActionEnum
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProgramEditFragment :
    BaseFragment<ProgramEditViewModel, FragmentProgramEditBinding>(R.layout.fragment_program_edit) {

    override val viewModel: ProgramEditViewModel by viewModels()

    private val actionAdapter by lazy { ActionAdapter() }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initObserver()
        initRecyclerView()
        initButton()
        initEditText()
    }

    /**
     * 初始化操作
     */
    private fun initObserver() {
        lifecycleScope.launch {
            viewModel.state.distinctUntilChanged().collect {
                when (it) {
                    is ProgramEditState.OnSwitchAction -> onSwitchAction(it.action)
                    is ProgramEditState.OnActionChange -> onActionChange(it.actionList)
                    is ProgramEditState.OnButtonChange -> onButtonChange(it.enable)
                }
            }
        }
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        binding.rc1.adapter = actionAdapter
        actionAdapter.setOnDeleteButtonClick {
            PopTip.show("已删除")
            viewModel.dispatch(ProgramEditIntent.OnDeleteAction(it))
        }
        arguments?.run {
            val programId = ProgramEditFragmentArgs.fromBundle(this).programId
            viewModel.dispatch(ProgramEditIntent.OnLoadActions(programId))
        }
    }

    /**
     * 初始化按钮
     */
    private fun initButton() {
        binding.btnBack.run {
            this.clickScale()
            this.setOnClickListener {
                findNavController().navigateUp()
            }
        }
        binding.btnAdd.setOnClickListener {
            viewModel.dispatch(ProgramEditIntent.OnAddAction)
        }
        val action = viewModel.uiState.value.action
        binding.btnAction.run {
            text = getActionEnum(action.mode).str
            setOnClickListener {
                val menuList = ActionEnum.values().map { it.str }
                BottomMenu.show(menuList)
                    .setOnIconChangeCallBack(object : OnIconChangeCallBack<BottomMenu>() {
                        override fun getIcon(
                            dialog: BottomMenu?,
                            index: Int,
                            menuText: String?
                        ): Int {
                            when (menuText) {
                                ActionEnum.BLOCKING_LIQUID.str -> return R.mipmap.ic_blocking_liquid
                                ActionEnum.ANTIBODY_ONE.str -> return R.mipmap.ic_antibody
                                ActionEnum.ANTIBODY_TWO.str -> return R.mipmap.ic_antibody
                                ActionEnum.WASHING.str -> return R.mipmap.ic_washing
                            }
                            return 0
                        }

                    })
                    .setOnMenuItemClickListener { _, text, index ->
                        binding.btnAction.text = text
                        viewModel.dispatch(ProgramEditIntent.OnSwitchAction(getActionEnum(index)))
                        false
                    }
            }
        }
    }

    /**
     * 初始化EditText
     */
    private fun initEditText() {
        binding.order.afterTextChange {
            val action = viewModel.uiState.value.action
            if (it.isNotEmpty()) {
                action.order = it.toInt()
            } else {
                action.order = 0
            }
            viewModel.dispatch(ProgramEditIntent.OnEditAction(action))
        }
        binding.time.afterTextChange {
            val action = viewModel.uiState.value.action
            if (it.isNotEmpty()) {
                action.time = it.toFloat()
            } else {
                action.time = 0f
            }
            viewModel.dispatch(ProgramEditIntent.OnEditAction(action))
        }
        binding.temperature.afterTextChange {
            val action = viewModel.uiState.value.action
            if (it.isNotEmpty()) {
                action.temperature = it.toFloat()
            } else {
                action.temperature = 0f
            }
            viewModel.dispatch(ProgramEditIntent.OnEditAction(action))
        }
        binding.liquidVolume.afterTextChange {
            val action = viewModel.uiState.value.action
            if (it.isNotEmpty()) {
                action.liquidVolume = it.toFloat()
            } else {
                action.liquidVolume = 0f
            }
            viewModel.dispatch(ProgramEditIntent.OnEditAction(action))
        }
        binding.count.afterTextChange {
            val action = viewModel.uiState.value.action
            if (it.isNotEmpty()) {
                action.count = it.toInt()
            } else {
                action.count = 0
            }
            viewModel.dispatch(ProgramEditIntent.OnEditAction(action))

        }
        onSwitchAction(getActionEnum(viewModel.uiState.value.action.mode))
    }

    /**
     * 切换Action操作
     * @param action [ActionEnum]
     */
    private fun onSwitchAction(action: ActionEnum) {
        if (action == ActionEnum.WASHING) {
            binding.run {
                inputCount.visibility = View.VISIBLE
                inputTime.hint = resources.getString(R.string.hint_time_min)
            }
        } else {
            binding.run {
                inputCount.visibility = View.GONE
                inputTime.hint = resources.getString(R.string.hint_time_hour)
            }
        }
    }

    /**
     * Action列表变化
     * @param actionList [List]<[Action]> Action列表
     */
    private fun onActionChange(actionList: List<Action>) {
        actionAdapter.submitList(actionList)
    }

    /**
     * 按钮状态变化
     * @param enable [Boolean] 是否可用
     */
    private fun onButtonChange(enable: Boolean) {
        binding.btnAdd.run {
            this.isEnabled = enable
            if (enable) {
                this.text = resources.getString(R.string.add)
            } else {
                this.text = resources.getString(R.string.add_ban)
            }
        }
    }

}