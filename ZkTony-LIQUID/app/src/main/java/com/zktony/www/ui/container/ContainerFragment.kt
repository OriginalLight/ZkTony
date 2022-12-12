package com.zktony.www.ui.container

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.databinding.FragmentContainerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContainerFragment :
    BaseFragment<ContainerViewModel, FragmentContainerBinding>(R.layout.fragment_container) {
    override val viewModel: ContainerViewModel by viewModels()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initView()
    }

    /**
     * 初始化view
     */
    private fun initView() {

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 3
            }

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> {
                        SampleFragment()
                    }
                    1 -> {
                        WashFragment()
                    }
                    2 -> {
                        ElutionFragment()
                    }
                    else -> {
                        SampleFragment()
                    }
                }
            }

        }

        TabLayoutMediator(
            binding.tabLayout, binding.viewPager, true, true
        ) { tab, position ->
            tab.text = when (position) {
                0 -> {
                    "样品区"
                }
                1 -> {
                    "洗涤区"
                }
                2 -> {
                    "洗脱区"
                }
                else -> {
                    "样品区"
                }
            }
        }.attach()
    }
}