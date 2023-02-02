package com.zktony.www.ui.work

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.extension.clickScale
import com.zktony.www.databinding.FragmentWorkFlowBinding
import com.zktony.www.ui.calibration.CalibrationDataFragmentArgs
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WorkFlowFragment :
    BaseFragment<WorkFlowViewModel, FragmentWorkFlowBinding>(R.layout.fragment_work_flow) {
    override val viewModel: WorkFlowViewModel by viewModels()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initView()
        initButton()
    }

    private fun initView() {
        arguments?.let {
            CalibrationDataFragmentArgs.fromBundle(it).id.run {
                if (this != "None") {
                    viewModel.init(this)
                }
            }
        }
    }

    private fun initButton() {
        binding.back.run {
            clickScale()
            setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }
}