package com.zktony.www.ui.work

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.databinding.FragmentWorkFlowBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WorkFlowFragment :
    BaseFragment<WorkFlowViewModel, FragmentWorkFlowBinding>(R.layout.fragment_work_flow) {
    override val viewModel: WorkFlowViewModel by viewModels()

    override fun onViewCreated(savedInstanceState: Bundle?) {
    }
}