package com.zktony.www.ui.admin

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.zktony.common.base.BaseFragment
import com.zktony.common.ext.clickScale
import com.zktony.common.ext.removeZero
import com.zktony.common.ext.setEqualText
import com.zktony.www.R
import com.zktony.www.data.local.room.entity.Container
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
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.container.collect {
                        binding.apply {
                            wasteY.setEqualText(it.wasteY.toString().removeZero())
                            wasteZ.setEqualText(it.wasteZ.toString().removeZero())
                            washY.setEqualText(it.washY.toString().removeZero())
                            washZ.setEqualText(it.washZ.toString().removeZero())
                            blockY.setEqualText(it.blockY.toString().removeZero())
                            blockZ.setEqualText(it.blockZ.toString().removeZero())
                            oneY.setEqualText(it.oneY.toString().removeZero())
                            oneZ.setEqualText(it.oneZ.toString().removeZero())
                            recycleOneZ.setEqualText(it.recycleOneZ.toString().removeZero())
                            twoY.setEqualText(it.twoY.toString().removeZero())
                            twoZ.setEqualText(it.twoZ.toString().removeZero())
                        }
                    }
                }
            }
        }
    }

    /**
     * 初始化按钮
     */
    private fun initButton() {
        binding.apply {
            with(back) {
                clickScale()
                setOnClickListener {
                    findNavController().navigateUp()
                }
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
            update.setOnClickListener {
                viewModel.update(
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
                    )
                )
            }
        }
    }
}