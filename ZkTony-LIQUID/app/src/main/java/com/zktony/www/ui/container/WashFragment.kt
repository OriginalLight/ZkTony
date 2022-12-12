package com.zktony.www.ui.container

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.databinding.FragmentWashBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WashFragment : BaseFragment<WashViewModel, FragmentWashBinding>(R.layout.fragment_wash) {

    override val viewModel: WashViewModel by viewModels()

    override fun onViewCreated(savedInstanceState: Bundle?) {

    }


}