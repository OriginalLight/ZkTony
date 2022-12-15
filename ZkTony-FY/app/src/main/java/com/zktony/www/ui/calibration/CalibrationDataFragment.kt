package com.zktony.www.ui.calibration

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.databinding.FragmentCalibrationDataBinding
import com.zktony.www.ui.program.ActionFragmentArgs

class CalibrationDataFragment :
    BaseFragment<CalibrationDataViewModel, FragmentCalibrationDataBinding>(R.layout.fragment_calibration_data) {
    override val viewModel: CalibrationDataViewModel by viewModels()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initRecycleView()
    }

    /**
     * 初始化列表
     */
    private fun initRecycleView() {
        arguments?.let {
            ActionFragmentArgs.fromBundle(it).id.run {
                if (this != "None") {
                    viewModel.initCali(this)
                }
            }
        }
    }
}