package com.zktony.www.ui.admin

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.clickNoRepeat
import com.zktony.core.ext.clickScale
import com.zktony.core.ext.removeZero
import com.zktony.core.ext.setEqualText
import com.zktony.www.R
import com.zktony.www.databinding.FragmentContainerBinding
import com.zktony.www.manager.SerialManager
import com.zktony.www.room.entity.Container
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContainerFragment :
    BaseFragment<ContainerViewModel, FragmentContainerBinding>(R.layout.fragment_container) {

    override val viewModel: ContainerViewModel by viewModel()

    private val serialManager: SerialManager by inject()

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
                launch {
                    serialManager.lock.collect {
                        binding.apply {
                            toWashY.isEnabled = !it
                            toWashZ.isEnabled = !it
                            toWasteY.isEnabled = !it
                            toWasteZ.isEnabled = !it
                            toBlockY.isEnabled = !it
                            toBlockZ.isEnabled = !it
                            toOneY.isEnabled = !it
                            toOneZ.isEnabled = !it
                            toRecycleOneZ.isEnabled = !it
                            toTwoY.isEnabled = !it
                            toTwoZ.isEnabled = !it
                            toZero.isEnabled = !it
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
                clickNoRepeat {
                    findNavController().navigateUp()
                }
            }
            toWashY.clickNoRepeat { viewModel.toWashY() }
            toWashZ.clickNoRepeat { viewModel.toWashZ() }
            toWasteY.clickNoRepeat { viewModel.toWasteY() }
            toWasteZ.clickNoRepeat { viewModel.toWasteZ() }
            toBlockY.clickNoRepeat { viewModel.toBlockY() }
            toBlockZ.clickNoRepeat { viewModel.toBlockZ() }
            toOneY.clickNoRepeat { viewModel.toOneY() }
            toOneZ.clickNoRepeat { viewModel.toOneZ() }
            toRecycleOneZ.clickNoRepeat { viewModel.toRecycleOneZ() }
            toTwoY.clickNoRepeat { viewModel.toTwoY() }
            toTwoZ.clickNoRepeat { viewModel.toTwoZ() }
            toZero.clickNoRepeat { viewModel.toZero() }
            update.clickNoRepeat {
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