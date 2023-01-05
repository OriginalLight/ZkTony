package com.zktony.www.ui.admin

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.extension.clickScale
import com.zktony.www.common.extension.removeZero
import com.zktony.www.data.model.Container
import com.zktony.www.databinding.FragmentContainerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContainerFragment :
    BaseFragment<ContainerViewModel, FragmentContainerBinding>(R.layout.fragment_container) {
    override val viewModel: ContainerViewModel by viewModels()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initButton()
    }

    /**
     * 初始化Flow收集器
     */
    private fun initFlowCollector() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.container.collect {
                    onContainerChange(it)
                }
            }
        }
    }

    /**
     * 初始化按钮
     */
    private fun initButton() {
        binding.run {
            back.run {
                clickScale()
                setOnClickListener {
                    findNavController().navigateUp()
                }
            }
            update.setOnClickListener {
                viewModel.replaceContainer(
                    Container().copy(
                        wasteY = binding.wasteY.text.toString().toFloat(),
                        wasteZ = binding.wasteZ.text.toString().toFloat(),
                        washY = binding.washY.text.toString().toFloat(),
                        washZ = binding.washZ.text.toString().toFloat(),
                        blockY = binding.blockY.text.toString().toFloat(),
                        blockZ = binding.blockZ.text.toString().toFloat(),
                        oneY = binding.oneY.text.toString().toFloat(),
                        oneZ = binding.oneZ.text.toString().toFloat(),
                        recycleOneZ = binding.recycleOneZ.text.toString().toFloat(),
                        twoY = binding.twoY.text.toString().toFloat(),
                        twoZ = binding.twoZ.text.toString().toFloat(),
                        extract = binding.extract.text.toString().toFloat(),
                    )
                )
            }
            toWashY.setOnClickListener { viewModel.toWashY() }
            toWashZ.setOnClickListener { viewModel.toWashZ() }
            toWasteY.setOnClickListener { viewModel.toWasteY() }
            toWasteZ.setOnClickListener { viewModel.toWasteZ() }
            toBlockY.setOnClickListener { viewModel.toBlockY() }
            toBlockZ.setOnClickListener { viewModel.toBlockZ() }
            toOneY.setOnClickListener { viewModel.toOneY() }
            toOneZ.setOnClickListener { viewModel.toOneZ() }
            toRecycleOneZ.setOnClickListener { viewModel.toRecycleOneZ() }
            toTwoY.setOnClickListener { viewModel.toTwoY() }
            toTwoZ.setOnClickListener { viewModel.toTwoZ() }
            toZero.setOnClickListener { viewModel.toZero() }
        }
    }

    /**
     * 容器数据变化
     * @param container [Container] 容器
     */
    private fun onContainerChange(container: Container) {
        binding.run {
            wasteY.setText(container.wasteY.toString().removeZero())
            wasteZ.setText(container.wasteZ.toString().removeZero())
            washY.setText(container.washY.toString().removeZero())
            washZ.setText(container.washZ.toString().removeZero())
            blockY.setText(container.blockY.toString().removeZero())
            blockZ.setText(container.blockZ.toString().removeZero())
            oneY.setText(container.oneY.toString().removeZero())
            oneZ.setText(container.oneZ.toString().removeZero())
            recycleOneZ.setText(container.recycleOneZ.toString().removeZero())
            twoY.setText(container.twoY.toString().removeZero())
            twoZ.setText(container.twoZ.toString().removeZero())
            extract.setText(container.extract.toString().removeZero())
        }
    }
}