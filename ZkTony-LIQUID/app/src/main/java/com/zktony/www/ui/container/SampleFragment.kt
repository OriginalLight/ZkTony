package com.zktony.www.ui.container

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.databinding.FragmentSampleBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SampleFragment :
    BaseFragment<SampleViewModel, FragmentSampleBinding>(R.layout.fragment_sample) {
    override val viewModel: SampleViewModel by viewModels()

    override fun onViewCreated(savedInstanceState: Bundle?) {

    }
}