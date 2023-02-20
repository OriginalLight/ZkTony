package com.zktony.www.ui.home

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.zktony.common.base.BaseFragment
import com.zktony.www.R
import com.zktony.www.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>(R.layout.fragment_home) {

    override val viewModel: HomeViewModel by viewModels()

    override fun onViewCreated(savedInstanceState: Bundle?) {
    }


}