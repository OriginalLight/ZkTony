package com.zktony.www.ui.admin

import android.os.Bundle
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.*
import com.zktony.www.R
import com.zktony.www.core.ext.collectLock
import com.zktony.www.databinding.FragmentContainerBinding
import com.zktony.www.data.entities.Container
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContainerFragment :
    BaseFragment<ContainerViewModel, FragmentContainerBinding>(R.layout.fragment_container) {

    override val viewModel: ContainerViewModel by viewModel()

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
                            wastePosition.setEqualText(it.wasteY.format())
                            wasteHeight.setEqualText(it.wasteZ.format())
                            washPosition.setEqualText(it.washY.format())
                            washHeight.setEqualText(it.washZ.format())
                            blockingLiquidPosition.setEqualText(it.blockY.format())
                            blockingLiquidHeight.setEqualText(it.blockZ.format())
                            antibodyOnePosition.setEqualText(it.oneY.format())
                            antibodyOneHeight.setEqualText(it.oneZ.format())
                            antibodyOneRecycle.setEqualText(it.recycleOneZ.format())
                            antibodyTwoPosition.setEqualText(it.twoY.format())
                            antibodyTwoHeight.setEqualText(it.twoZ.format())
                        }
                    }
                }
                launch {
                    collectLock {
                        binding.apply {
                            moveWastePosition.isEnabled = !it
                            moveWasteHeight.isEnabled = !it
                            moveWashPosition.isEnabled = !it
                            moveWashHeight.isEnabled = !it
                            moveBlockingLiquidPosition.isEnabled = !it
                            moveBlockingLiquidHeight.isEnabled = !it
                            moveAntibodyOnePosition.isEnabled = !it
                            moveAntibodyOneHeight.isEnabled = !it
                            moveAntibodyOneRecycle.isEnabled = !it
                            moveAntibodyTwoPosition.isEnabled = !it
                            moveAntibodyTwoHeight.isEnabled = !it
                            zero.isEnabled = !it
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
            moveWashPosition.clickNoRepeat { viewModel.toWashY() }
            moveWashHeight.clickNoRepeat { viewModel.toWashZ() }
            moveWastePosition.clickNoRepeat { viewModel.toWasteY() }
            moveWasteHeight.clickNoRepeat { viewModel.toWasteZ() }
            moveBlockingLiquidPosition.clickNoRepeat { viewModel.toBlockY() }
            moveBlockingLiquidHeight.clickNoRepeat { viewModel.toBlockZ() }
            moveAntibodyOnePosition.clickNoRepeat { viewModel.toOneY() }
            moveAntibodyOneHeight.clickNoRepeat { viewModel.toOneZ() }
            moveAntibodyOneRecycle.clickNoRepeat { viewModel.toRecycleOneZ() }
            moveAntibodyTwoPosition.clickNoRepeat { viewModel.toTwoY() }
            moveAntibodyTwoHeight.clickNoRepeat { viewModel.toTwoZ() }
            zero.clickNoRepeat { viewModel.toZero() }
            update.clickNoRepeat {
                viewModel.update(
                    Container().copy(
                        wasteY = binding.wastePosition.text.toString().toFloat(),
                        wasteZ = binding.wasteHeight.text.toString().toFloat(),
                        washY = binding.washPosition.text.toString().toFloat(),
                        washZ = binding.washHeight.text.toString().toFloat(),
                        blockY = binding.blockingLiquidPosition.text.toString().toFloat(),
                        blockZ = binding.blockingLiquidHeight.text.toString().toFloat(),
                        oneY = binding.antibodyOnePosition.text.toString().toFloat(),
                        oneZ = binding.antibodyOneHeight.text.toString().toFloat(),
                        recycleOneZ = binding.antibodyOneRecycle.text.toString().toFloat(),
                        twoY = binding.antibodyTwoPosition.text.toString().toFloat(),
                        twoZ = binding.antibodyTwoHeight.text.toString().toFloat(),
                    )
                )
            }
        }
    }
}