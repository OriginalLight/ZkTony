package com.zktony.www.ui.program

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.extension.clickScale
import com.zktony.www.databinding.FragmentZmBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ZmFragment : BaseFragment<ZmViewModel, FragmentZmBinding>(R.layout.fragment_zm) {

    override val viewModel: ZmViewModel by viewModels()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initButton()
    }

    /**
     * 初始化按钮
     */
    private fun initButton() {
        binding.back.run {
            clickScale()
            setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }
}