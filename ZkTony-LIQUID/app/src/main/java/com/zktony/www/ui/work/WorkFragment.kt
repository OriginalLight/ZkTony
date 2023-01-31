package com.zktony.www.ui.work

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.databinding.FragmentWorkBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WorkFragment : BaseFragment<WorkViewModel, FragmentWorkBinding>(R.layout.fragment_work) {
    override val viewModel: WorkViewModel by viewModels()

    override fun onViewCreated(savedInstanceState: Bundle?) {

    }
}