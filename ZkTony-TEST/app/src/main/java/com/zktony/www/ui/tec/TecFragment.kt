package com.zktony.www.ui.tec

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.databinding.FragmentTecBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TecFragment : BaseFragment<TecViewModel, FragmentTecBinding>(R.layout.fragment_tec) {

    override val viewModel: TecViewModel by viewModels()

    @Inject
    lateinit var appViewModel: AppViewModel

    override fun onViewCreated(savedInstanceState: Bundle?) {
    }


}