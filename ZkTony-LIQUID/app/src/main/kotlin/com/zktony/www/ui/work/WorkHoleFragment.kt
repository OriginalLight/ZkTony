package com.zktony.www.ui.work

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.extension.clickScale
import com.zktony.www.databinding.FragmentWorkHoleBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WorkHoleFragment :
    BaseFragment<WorkHoleViewModel, FragmentWorkHoleBinding>(R.layout.fragment_work_hole) {
    override val viewModel: WorkHoleViewModel by viewModels()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initButton()
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