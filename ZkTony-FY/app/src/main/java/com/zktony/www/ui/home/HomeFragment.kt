package com.zktony.www.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.extension.clickScale
import com.zktony.www.common.room.entity.getActionEnum
import com.zktony.www.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>(R.layout.fragment_home) {

    override val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var appViewModel: AppViewModel

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initObserver()
        initTextView()
        initButton()
    }

    /**
     * 初始化观察者
     */
    private fun initObserver() {
        lifecycleScope.launch {
            viewModel.state.collect {
                when (it) {
                    is HomeState.OnSwitchProgram -> onSwitchProgram(it.index, it.module)
                    is HomeState.OnLoadProgram -> onLoadProgram(it.uiState)
                    is HomeState.OnButtonChange -> onButtonChange(it.module)
                    is HomeState.OnRestCallBack -> onRestCallBack(it.success)
                    is HomeState.OnPause -> onBtnPause()
                    is HomeState.OnInsulating -> onInsulating()
                    is HomeState.OnDashBoardChange -> onDashBoardChange(it.module)
                }
            }
        }
    }

    /**
     * 初始化文本框
     */
    @SuppressLint("SetTextI18n")
    private fun initTextView() {
        val uiState = viewModel.uiState.value
        binding.a.run {
            tvActions.setOnClickListener {
                PopTip.show((it as TextView).text)
            }
            dashState.text = if (uiState.moduleA.isRunning) "运行中" else "已就绪"
            dashAction.text =
                if (uiState.dashBoardA.currentAction.order == 0) "/" else uiState.dashBoardA.currentAction.order.toString() + " " + getModuleEnum(
                    uiState.dashBoardA.currentAction.mode
                ).value
            dashTemp.text = uiState.dashBoardA.temperature
            dashTime.text = uiState.dashBoardA.time

        }
        binding.b.run {
            tvActions.setOnClickListener {
                PopTip.show((it as TextView).text)
            }
            dashState.text = if (uiState.moduleB.isRunning) "运行中" else "已就绪"
            dashAction.text =
                if (uiState.dashBoardB.currentAction.order == 0) "/" else uiState.dashBoardB.currentAction.order.toString() + " " + getActionEnum(
                    uiState.dashBoardB.currentAction.mode
                ).value
            dashTemp.text = uiState.dashBoardB.temperature
            dashTime.text = uiState.dashBoardB.time
        }
        binding.c.run {
            tvActions.setOnClickListener {
                PopTip.show((it as TextView).text)
            }
            dashState.text = if (uiState.moduleC.isRunning) "运行中" else "已就绪"
            dashAction.text =
                if (uiState.dashBoardA.currentAction.order == 0) "/" else uiState.dashBoardC.currentAction.order.toString() + " " + getActionEnum(
                    uiState.dashBoardC.currentAction.mode
                ).value
            dashTemp.text = uiState.dashBoardC.temperature
            dashTime.text = uiState.dashBoardC.time
        }
        binding.d.run {
            tvActions.setOnClickListener {
                PopTip.show((it as TextView).text)
            }
            dashState.text = if (uiState.moduleD.isRunning) "运行中" else "已就绪"
            dashAction.text =
                if (uiState.dashBoardD.currentAction.order == 0) "/" else uiState.dashBoardD.currentAction.order.toString() + " " + getActionEnum(
                    uiState.dashBoardD.currentAction.mode
                ).value
            dashTemp.text = uiState.dashBoardD.temperature
            dashTime.text = uiState.dashBoardD.time
        }
    }

    /**
     * 初始化按钮
     */
    private fun initButton() {
        val uiState = viewModel.uiState.value
        binding.a.run {
            btnStart.run {
                isEnabled = uiState.moduleA.btnStart.enable
                visibility = uiState.moduleA.btnStart.visibility
                setOnClickListener {
                    this@HomeFragment.viewModel.start(ModuleEnum.A)
                }
            }
            btnStop.run {
                isEnabled = uiState.moduleA.btnStop.enable
                visibility = uiState.moduleA.btnStop.visibility
                setOnClickListener {
                    PopTip.show(R.mipmap.ic_stop, "长按停止")
                }
                setOnLongClickListener {
                    this@HomeFragment.viewModel.stop(ModuleEnum.A)
                    true
                }
            }
            btnProgram.run {
                isClickable = uiState.moduleA.btnProgram.isClickable
                setOnClickListener {
                    showSelectProgramDialog(ModuleEnum.A)
                }
                onSwitchProgram(uiState.moduleA.index, ModuleEnum.A)
            }
        }
        binding.b.run {
            btnStart.run {
                isEnabled = uiState.moduleB.btnStart.enable
                visibility = uiState.moduleB.btnStart.visibility
                setOnClickListener {
                    this@HomeFragment.viewModel.start(ModuleEnum.B)
                }
            }
            btnStop.run {
                isEnabled = uiState.moduleB.btnStop.enable
                visibility = uiState.moduleB.btnStop.visibility
                setOnClickListener {
                    PopTip.show(R.mipmap.ic_stop, "长按停止")
                }
                setOnLongClickListener {
                    this@HomeFragment.viewModel.stop(ModuleEnum.B)
                    true
                }
            }
            btnProgram.run {
                isClickable = uiState.moduleB.btnProgram.isClickable
                setOnClickListener {
                    showSelectProgramDialog(ModuleEnum.B)
                }
                onSwitchProgram(uiState.moduleB.index, ModuleEnum.B)
            }
        }
        binding.c.run {
            btnStart.run {
                isEnabled = uiState.moduleC.btnStart.enable
                visibility = uiState.moduleC.btnStart.visibility
                setOnClickListener {
                    this@HomeFragment.viewModel.start(ModuleEnum.C)
                }
            }
            btnStop.run {
                isEnabled = uiState.moduleC.btnStop.enable
                visibility = uiState.moduleC.btnStop.visibility
                setOnClickListener {
                    PopTip.show(R.mipmap.ic_stop, "长按停止")
                }
                setOnLongClickListener {
                    this@HomeFragment.viewModel.stop(ModuleEnum.C)
                    true
                }
            }
            btnProgram.run {
                isClickable = uiState.moduleC.btnProgram.isClickable
                setOnClickListener {
                    showSelectProgramDialog(ModuleEnum.C)
                }
                onSwitchProgram(uiState.moduleC.index, ModuleEnum.C)
            }
        }
        binding.d.run {
            btnStart.run {
                isEnabled = uiState.moduleD.btnStart.enable
                visibility = uiState.moduleD.btnStart.visibility
                setOnClickListener {
                    this@HomeFragment.viewModel.start(ModuleEnum.D)
                }
            }
            btnStop.run {
                isEnabled = uiState.moduleD.btnStop.enable
                visibility = uiState.moduleD.btnStop.visibility
                setOnClickListener {
                    PopTip.show(R.mipmap.ic_stop, "长按停止")
                }
                setOnLongClickListener {
                    this@HomeFragment.viewModel.stop(ModuleEnum.D)
                    true
                }
            }
            btnProgram.run {
                setOnClickListener {
                    showSelectProgramDialog(ModuleEnum.D)
                }
                onSwitchProgram(uiState.moduleD.index, ModuleEnum.D)
                run { isClickable = uiState.moduleD.btnProgram.isClickable }
            }
        }
        binding.e.run {
            btnReset.run {
                clickScale()
                setOnClickListener {
                    PopTip.show(R.mipmap.ic_reset, "长按复位")
                }
                setOnLongClickListener {
                    PopTip.show(R.mipmap.ic_reset, "复位-已下发")
                    this@HomeFragment.viewModel.reset()
                    true
                }
            }
            btnPause.run {
                clickScale()
                setOnClickListener {
                    if (this@HomeFragment.viewModel.uiState.value.btnPause.isRunning) {
                        PopTip.show(R.mipmap.ic_stop, "已继续摇床，再次点击暂停")
                    } else {
                        PopTip.show(R.mipmap.ic_stop, "已暂停摇床，再次点击继续")
                    }
                    this@HomeFragment.viewModel.pause()
                }
            }
            btnInsulating.run {
                clickScale()
                setOnClickListener {
                    if (this@HomeFragment.viewModel.uiState.value.btnInsulating.isRunning) {
                        PopTip.show(R.mipmap.ic_insulating, "已取消保温，再次点击开启")
                    } else {
                        PopTip.show(R.mipmap.ic_insulating, "抗体保温中，再次点击取消")
                    }
                    this@HomeFragment.viewModel.insulating()
                }
            }
            onBtnPause()
            onInsulating()
        }
    }

    /**
     * 选择程序
     * @param index [Int] 选中的下标
     * @param module [ModuleEnum] 模块
     */
    private fun onSwitchProgram(index: Int, module: ModuleEnum) {
        val programList = viewModel.uiState.value.programList
        when (module) {
            ModuleEnum.A -> {
                binding.a.run {
                    if (index != -1) {
                        btnProgram.text = programList[index].name
                        tvActions.text = programList[index].actions
                    } else {
                        btnProgram.text = ""
                        tvActions.text = ""
                    }
                }
            }
            ModuleEnum.B -> {
                binding.b.run {
                    if (index != -1) {
                        btnProgram.text = programList[index].name
                        tvActions.text = programList[index].actions
                    } else {
                        btnProgram.text = ""
                        tvActions.text = ""
                    }
                }
            }
            ModuleEnum.C -> {
                binding.c.run {
                    if (index != -1) {
                        btnProgram.text = programList[index].name
                        tvActions.text = programList[index].actions
                    } else {
                        btnProgram.text = ""
                        tvActions.text = ""
                    }
                }
            }
            ModuleEnum.D -> {
                binding.d.run {
                    if (index != -1) {
                        btnProgram.text = programList[index].name
                        tvActions.text = programList[index].actions
                    } else {
                        btnProgram.text = ""
                        tvActions.text = ""
                    }
                }
            }
        }
    }

    /**
     * 加载程序列表
     * @param uiState [HomeUiState]
     */
    private fun onLoadProgram(uiState: HomeUiState) {
        onSwitchProgram(uiState.moduleA.index, ModuleEnum.A)
        onSwitchProgram(uiState.moduleB.index, ModuleEnum.B)
        onSwitchProgram(uiState.moduleC.index, ModuleEnum.C)
        onSwitchProgram(uiState.moduleD.index, ModuleEnum.D)
        onButtonChange(ModuleEnum.A)
        onButtonChange(ModuleEnum.B)
        onButtonChange(ModuleEnum.C)
        onButtonChange(ModuleEnum.D)
    }

    /**
     * 按钮状态变化
     * @param module [ModuleEnum] 模块
     */
    private fun onButtonChange(module: ModuleEnum) {
        val uiState = viewModel.uiState.value
        when (module) {
            ModuleEnum.A -> {
                binding.a.run {
                    btnStart.run {
                        isEnabled = uiState.moduleA.btnStart.enable
                        visibility = uiState.moduleA.btnStart.visibility
                    }
                    btnStop.run {
                        isEnabled = uiState.moduleA.btnStop.enable
                        visibility = uiState.moduleA.btnStop.visibility
                    }
                    btnProgram.run {
                        isClickable = uiState.moduleA.btnProgram.isClickable
                    }
                }
            }
            ModuleEnum.B -> {
                binding.b.run {
                    btnStart.run {
                        isEnabled = uiState.moduleB.btnStart.enable
                        visibility = uiState.moduleB.btnStart.visibility
                    }
                    btnStop.run {
                        isEnabled = uiState.moduleB.btnStop.enable
                        visibility = uiState.moduleB.btnStop.visibility
                    }
                    btnProgram.run {
                        isClickable = uiState.moduleB.btnProgram.isClickable
                    }
                }
            }
            ModuleEnum.C -> {
                binding.c.run {
                    btnStart.run {
                        isEnabled = uiState.moduleC.btnStart.enable
                        visibility = uiState.moduleC.btnStart.visibility
                    }
                    btnStop.run {
                        isEnabled = uiState.moduleC.btnStop.enable
                        visibility = uiState.moduleC.btnStop.visibility
                    }
                    btnProgram.run {
                        isClickable = uiState.moduleC.btnProgram.isClickable
                    }
                }
            }
            ModuleEnum.D -> {
                binding.d.run {
                    btnStart.run {
                        isEnabled = uiState.moduleD.btnStart.enable
                        visibility = uiState.moduleD.btnStart.visibility
                    }
                    btnStop.run {
                        isEnabled = uiState.moduleD.btnStop.enable
                        visibility = uiState.moduleD.btnStop.visibility
                    }
                    btnProgram.run {
                        isClickable = uiState.moduleD.btnProgram.isClickable
                    }
                }
            }
        }
    }

    /**
     * 摇床暂停
     */
    private fun onBtnPause() {
        val uiState = viewModel.uiState.value
        binding.e.run {
            btnPause.run {
                setBackgroundResource(uiState.btnPause.background)
            }
            tvPause.run {
                text = uiState.btnPause.text
                setTextColor(ContextCompat.getColor(context, uiState.btnPause.textColor))
            }
        }
    }

    /**
     * 抗体保温
     */
    private fun onInsulating() {
        val uiState = viewModel.uiState.value
        binding.e.tvInsulating.run {
            text = uiState.btnInsulating.text
            setTextColor(ContextCompat.getColor(context, uiState.btnInsulating.textColor))
        }
    }

    /**
     * Dashboard变化
     * @param module [ModuleEnum] 模块
     */
    private fun onDashBoardChange(module: ModuleEnum) {
        val uiState = viewModel.uiState.value
        when (module) {
            ModuleEnum.A -> {
                binding.a.run {
                    dashState.text = if (uiState.moduleA.isRunning) "运行中" else "已就绪"
                    dashAction.text =
                        if (uiState.dashBoardA.currentAction.order == 0) "/" else uiState.dashBoardA.currentAction.order.toString() + " " + getActionEnum(
                            uiState.dashBoardA.currentAction.mode
                        ).value
                    dashTemp.text = uiState.dashBoardA.temperature
                    dashTime.text = uiState.dashBoardA.time
                }
            }
            ModuleEnum.B -> {
                binding.b.run {
                    dashState.text = if (uiState.moduleB.isRunning) "运行中" else "已就绪"
                    dashAction.text =
                        if (uiState.dashBoardB.currentAction.order == 0) "/" else uiState.dashBoardB.currentAction.order.toString() + " " + getActionEnum(
                            uiState.dashBoardB.currentAction.mode
                        ).value
                    dashTemp.text = uiState.dashBoardB.temperature
                    dashTime.text = uiState.dashBoardB.time
                }
            }
            ModuleEnum.C -> {
                binding.c.run {
                    dashState.text = if (uiState.moduleC.isRunning) "运行中" else "已就绪"
                    dashAction.text =
                        if (uiState.dashBoardC.currentAction.order == 0) "/" else uiState.dashBoardC.currentAction.order.toString() + " " + getActionEnum(
                            uiState.dashBoardC.currentAction.mode
                        ).value
                    dashTemp.text = uiState.dashBoardC.temperature
                    dashTime.text = uiState.dashBoardC.time
                }
            }
            ModuleEnum.D -> {
                binding.d.run {
                    dashState.text = if (uiState.moduleD.isRunning) "运行中" else "已就绪"
                    dashAction.text =
                        if (uiState.dashBoardD.currentAction.order == 0) "/" else uiState.dashBoardD.currentAction.order.toString() + " " + getActionEnum(
                            uiState.dashBoardD.currentAction.mode
                        ).value
                    dashTemp.text = uiState.dashBoardD.temperature
                    dashTime.text = uiState.dashBoardD.time
                }
            }
        }
    }

    /**
     * 复位回调
     * @param success [Boolean] 是否成功
     */
    private fun onRestCallBack(success: Boolean) {
        if (success) {
            PopTip.show(R.mipmap.ic_reset, "复位成功")
        } else {
            PopTip.show(R.mipmap.ic_reset, "复位失败")
        }
    }

    /**
     * 选择程序
     * @param module [ModuleEnum] 模块
     */
    private fun showSelectProgramDialog(module: ModuleEnum) {
        val uiState = viewModel.uiState.value
        val menuList = uiState.programList.map { it.name }
        if (menuList.isNotEmpty()) {
            BottomMenu.show(menuList).setMessage("请选择程序")
                .setOnMenuItemClickListener { _, _, index ->
                    viewModel.onSwitchProgram(index, module)
                    false
                }
        }
    }

}