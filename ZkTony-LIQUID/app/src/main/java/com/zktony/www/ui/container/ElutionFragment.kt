package com.zktony.www.ui.container

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.databinding.FragmentElutionBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ElutionFragment :
    BaseFragment<ElutionViewModel, FragmentElutionBinding>(R.layout.fragment_elution) {
    override val viewModel: ElutionViewModel by viewModels()

    override fun onViewCreated(savedInstanceState: Bundle?) {

    }


}