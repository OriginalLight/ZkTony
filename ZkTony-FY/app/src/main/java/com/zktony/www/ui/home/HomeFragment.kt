package com.zktony.www.ui.home

import android.os.Bundle
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.extension.clickScale
import com.zktony.www.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment :
    BaseFragment<HomeViewModel, FragmentHomeBinding>(R.layout.fragment_home) {

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
                }
            }
        }
    }

    /**
     * 初始化文本框
     */
    private fun initTextView() {
        binding.a.tvActions.setOnClickListener {
            PopTip.show((it as TextView).text)
        }
        binding.b.tvActions.setOnClickListener {
            PopTip.show((it as TextView).text)
        }
        binding.c.tvActions.setOnClickListener {
            PopTip.show((it as TextView).text)
        }
        binding.d.tvActions.setOnClickListener {
            PopTip.show((it as TextView).text)
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
                    this@HomeFragment.viewModel.dispatch(HomeIntent.OnStart(ModuleEnum.A))
                }
            }
            btnStop.run {
                isEnabled = uiState.moduleA.btnStop.enable
                visibility = uiState.moduleA.btnStop.visibility
                setOnClickListener {
                    PopTip.show(R.mipmap.ic_stop, "长按停止")
                }
                setOnLongClickListener {
                    this@HomeFragment.viewModel.dispatch(HomeIntent.OnStop(ModuleEnum.A))
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
                    this@HomeFragment.viewModel.dispatch(HomeIntent.OnStart(ModuleEnum.B))
                }
            }
            btnStop.run {
                isEnabled = uiState.moduleB.btnStop.enable
                visibility = uiState.moduleB.btnStop.visibility
                setOnClickListener {
                    PopTip.show(R.mipmap.ic_stop, "长按停止")
                }
                setOnLongClickListener {
                    this@HomeFragment.viewModel.dispatch(HomeIntent.OnStop(ModuleEnum.B))
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
                    this@HomeFragment.viewModel.dispatch(HomeIntent.OnStart(ModuleEnum.C))
                }
            }
            btnStop.run {
                isEnabled = uiState.moduleC.btnStop.enable
                visibility = uiState.moduleC.btnStop.visibility
                setOnClickListener {
                    PopTip.show(R.mipmap.ic_stop, "长按停止")
                }
                setOnLongClickListener {
                    this@HomeFragment.viewModel.dispatch(HomeIntent.OnStop(ModuleEnum.C))
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
                    this@HomeFragment.viewModel.dispatch(HomeIntent.OnStart(ModuleEnum.D))
                }
            }
            btnStop.run {
                isEnabled = uiState.moduleD.btnStop.enable
                visibility = uiState.moduleD.btnStop.visibility
                setOnClickListener {
                    PopTip.show(R.mipmap.ic_stop, "长按停止")
                }
                setOnLongClickListener {
                    this@HomeFragment.viewModel.dispatch(HomeIntent.OnStop(ModuleEnum.D))
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
                    this@HomeFragment.viewModel.dispatch(HomeIntent.OnReset)
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
                    this@HomeFragment.viewModel.dispatch(HomeIntent.OnPause)
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
                    this@HomeFragment.viewModel.dispatch(HomeIntent.OnInsulating)
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
        val uiState = viewModel.uiState.value
        when (module) {
            ModuleEnum.A -> {
                binding.a.run {
                    if (index != -1) {
                        btnProgram.text = uiState.programList[index].name
                        tvActions.text = uiState.programList[index].actions
                    } else {
                        btnProgram.text = ""
                        tvActions.text = ""
                    }
                }
            }
            ModuleEnum.B -> {
                binding.b.run {
                    if (index != -1) {
                        btnProgram.text = uiState.programList[index].name
                        tvActions.text = uiState.programList[index].actions
                    } else {
                        btnProgram.text = ""
                        tvActions.text = ""
                    }
                }
            }
            ModuleEnum.C -> {
                binding.c.run {
                    if (index != -1) {
                        btnProgram.text = uiState.programList[index].name
                        tvActions.text = uiState.programList[index].actions
                    } else {
                        btnProgram.text = ""
                        tvActions.text = ""
                    }
                }
            }
            ModuleEnum.D -> {
                binding.d.run {
                    if (index != -1) {
                        btnProgram.text = uiState.programList[index].name
                        tvActions.text = uiState.programList[index].actions
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
            BottomMenu.show(menuList)
                .setMessage("请选择程序")
                .setOnMenuItemClickListener { _, _, index ->
                    when (module) {
                        ModuleEnum.A -> {
                            viewModel.dispatch(HomeIntent.OnSwitchProgram(index, module))
                        }
                        ModuleEnum.B -> {
                            viewModel.dispatch(HomeIntent.OnSwitchProgram(index, module))
                        }
                        ModuleEnum.C -> {
                            viewModel.dispatch(HomeIntent.OnSwitchProgram(index, module))
                        }
                        ModuleEnum.D -> {
                            viewModel.dispatch(HomeIntent.OnSwitchProgram(index, module))
                        }
                    }
                    false
                }
        }
    }

}