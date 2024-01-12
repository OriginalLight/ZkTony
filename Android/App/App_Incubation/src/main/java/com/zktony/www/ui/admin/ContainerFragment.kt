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
            moveWashPosition.clickNoRepeat {
                val washY = binding.washPosition.text.toString().toFloatOrNull() ?: 0f
                viewModel.mveToY(washY)
            }
            moveWashHeight.clickNoRepeat {
                val washY = binding.washPosition.text.toString().toFloatOrNull() ?: 0f
                val washZ = binding.washHeight.text.toString().toFloatOrNull() ?: 0f
                viewModel.mveToZ(washY, washZ)
            }
            moveWastePosition.clickNoRepeat {
                val wasteY = binding.wastePosition.text.toString().toFloatOrNull() ?: 0f
                viewModel.mveToY(wasteY)
            }
            moveWasteHeight.clickNoRepeat {
                val wasteY = binding.wastePosition.text.toString().toFloatOrNull() ?: 0f
                val wasteZ = binding.wasteHeight.text.toString().toFloatOrNull() ?: 0f
                viewModel.mveToZ(wasteY, wasteZ)
            }
            moveBlockingLiquidPosition.clickNoRepeat {
                val blockY = binding.blockingLiquidPosition.text.toString().toFloatOrNull() ?: 0f
                viewModel.mveToY(blockY)
            }
            moveBlockingLiquidHeight.clickNoRepeat {
                val blockY = binding.blockingLiquidPosition.text.toString().toFloatOrNull() ?: 0f
                val blockZ = binding.blockingLiquidHeight.text.toString().toFloatOrNull() ?: 0f
                viewModel.mveToZ(blockY, blockZ)
            }
            moveAntibodyOnePosition.clickNoRepeat {
                val oneY = binding.antibodyOnePosition.text.toString().toFloatOrNull() ?: 0f
                viewModel.mveToY(oneY)
            }
            moveAntibodyOneHeight.clickNoRepeat {
                val oneY = binding.antibodyOnePosition.text.toString().toFloatOrNull() ?: 0f
                val oneZ = binding.antibodyOneHeight.text.toString().toFloatOrNull() ?: 0f
                viewModel.mveToZ(oneY, oneZ)
            }
            moveAntibodyOneRecycle.clickNoRepeat {
                val oneY = binding.antibodyOnePosition.text.toString().toFloatOrNull() ?: 0f
                val oneZ = binding.antibodyOneRecycle.text.toString().toFloatOrNull() ?: 0f
                viewModel.mveToZ(oneY, oneZ)
            }
            moveAntibodyTwoPosition.clickNoRepeat {
                val twoY = binding.antibodyTwoPosition.text.toString().toFloatOrNull() ?: 0f
                viewModel.mveToY(twoY)
            }
            moveAntibodyTwoHeight.clickNoRepeat {
                val twoY = binding.antibodyTwoPosition.text.toString().toFloatOrNull() ?: 0f
                val twoZ = binding.antibodyTwoHeight.text.toString().toFloatOrNull() ?: 0f
                viewModel.mveToZ(twoY, twoZ)
            }
            zero.clickNoRepeat { viewModel.mveToY(0f) }

            update.clickNoRepeat {
                viewModel.update(
                    Container().copy(
                        wasteY = binding.wastePosition.text.toString().toFloatOrNull() ?: 0f,
                        wasteZ = binding.wasteHeight.text.toString().toFloatOrNull() ?: 0f,
                        washY = binding.washPosition.text.toString().toFloatOrNull() ?: 0f,
                        washZ = binding.washHeight.text.toString().toFloatOrNull() ?: 0f,
                        blockY = binding.blockingLiquidPosition.text.toString().toFloatOrNull() ?: 0f,
                        blockZ = binding.blockingLiquidHeight.text.toString().toFloatOrNull() ?: 0f,
                        oneY = binding.antibodyOnePosition.text.toString().toFloatOrNull() ?: 0f,
                        oneZ = binding.antibodyOneHeight.text.toString().toFloatOrNull() ?: 0f,
                        recycleOneZ = binding.antibodyOneRecycle.text.toString().toFloatOrNull() ?: 0f,
                        twoY = binding.antibodyTwoPosition.text.toString().toFloatOrNull() ?: 0f,
                        twoZ = binding.antibodyTwoHeight.text.toString().toFloatOrNull() ?: 0f,
                    )
                )
            }
        }
    }
}