package com.zktony.www.ui.calibration

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.kongzue.dialogx.dialogs.PopMenu
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnIconChangeCallBack
import com.kongzue.dialogx.util.TextInfo
import com.zktony.www.R
import com.zktony.www.adapter.CalibrationDataAdapter
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.extension.clickScale
import com.zktony.www.common.room.entity.CalibrationData
import com.zktony.www.databinding.FragmentCalibrationDataBinding
import com.zktony.www.serial.SerialManager
import com.zktony.www.ui.program.ActionFragmentArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CalibrationDataFragment :
    BaseFragment<CalibrationDataViewModel, FragmentCalibrationDataBinding>(R.layout.fragment_calibration_data) {
    override val viewModel: CalibrationDataViewModel by viewModels()

    private val adapter by lazy { CalibrationDataAdapter() }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initRecycleView()
        initButton()
    }

    /**
     * 初始化Flow收集器
     */
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.caliDataList.collect { adapter.submitList(it) } }
                launch {
                    viewModel.motorId.collect {
                        binding.select.text = listOf("泵一", "泵二", "泵三", "泵四", "泵五")[it - 3]
                    }
                }
                launch {
                    SerialManager.instance.runtimeLock.collect {
                        binding.addLiquid.isEnabled = !it
                    }
                }
            }
        }
    }

    /**
     * 初始化列表
     */
    private fun initRecycleView() {
        binding.recycleView.adapter = adapter
        adapter.setOnDeleteButtonClick {
            viewModel.delete(it)
        }
        arguments?.let {
            ActionFragmentArgs.fromBundle(it).id.run {
                if (this != "None") {
                    viewModel.initCali(this)
                }
            }
        }
    }

    /**
     * 初始化按钮
     */
    private fun initButton() {
        binding.back.run {
            clickScale()
            setOnClickListener {
                findNavController().navigateUp()
            }
        }
        binding.select.setOnClickListener {
            val menuList = listOf("泵一", "泵二", "泵三", "泵四", "泵五")
            PopMenu.show(menuList).setMenuTextInfo(TextInfo().apply {
                gravity = Gravity.CENTER
                fontSize = 16
            }).setOnIconChangeCallBack(object : OnIconChangeCallBack<PopMenu>(true) {
                override fun getIcon(dialog: PopMenu?, index: Int, menuText: String?): Int {
                    return R.mipmap.ic_pump
                }
            }).setOnMenuItemClickListener { _, _, index ->
                viewModel.selectMotor(index + 3)
                binding.select.text = menuList[index]
                false
            }.width = 300
        }
        binding.save.setOnClickListener {
            val volume = binding.volume.text.toString()
            val actualVolume = binding.actualVolume.text.toString()
            if (volume.isEmpty() || actualVolume.isEmpty()) {
                PopTip.show("请填写完整数据")
                return@setOnClickListener
            }
            viewModel.add(
                CalibrationData().copy(
                    volume = volume.toFloat(),
                    actualVolume = actualVolume.toFloat(),
                )
            )
        }
        binding.addLiquid.setOnClickListener {
            val volume = binding.volume.text.toString()
            if (volume.isEmpty()) {
                PopTip.show("请填写完整数据")
                return@setOnClickListener
            }
            viewModel.addLiquid(volume.toFloat())
        }
    }
}