package com.zktony.www.ui.home

import android.os.Bundle
import com.zktony.core.base.BaseFragment
import com.zktony.www.R
import com.zktony.www.databinding.FragmentHomeBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>(R.layout.fragment_home) {

    override val viewModel: HomeViewModel by viewModel()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        viewModel.init()
    }

}