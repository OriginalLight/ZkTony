package com.zktony.www.ui.container

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.zktony.common.base.BaseFragment
import com.zktony.www.R
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
                return 2
            }

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> {
                        PlateFragment()
                    }
                    1 -> {
                        WashFragment()
                    }
                    else -> {
                        PlateFragment()
                    }
                }
            }
        }

        TabLayoutMediator(
            binding.tabLayout, binding.viewPager, true, true
        ) { tab, position ->
            tab.text = when (position) {
                0 -> {
                    "加液板"
                }
                1 -> {
                    "洗涤区"
                }
                else -> {
                    "加液板"
                }
            }
        }.attach()
    }
}