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
                return 5
            }

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> {
                        PlateOneFragment()
                    }
                    1 -> {
                        PlateTwoFragment()
                    }
                    2 -> {
                        PlateThreeFragment()
                    }
                    3 -> {
                        PlateFourFragment()
                    }
                    4 -> {
                        WashFragment()
                    }
                    else -> {
                        PlateOneFragment()
                    }
                }
            }
        }

        TabLayoutMediator(
            binding.tabLayout, binding.viewPager, true, true
        ) { tab, position ->
            tab.text = when (position) {
                0 -> {
                    "一号板"
                }
                1 -> {
                    "二号板"
                }
                2 -> {
                    "三号板"
                }
                3 -> {
                    "四号板"
                }
                4 -> {
                    "洗涤区"
                }
                else -> {
                    "一号板"
                }
            }
        }.attach()
    }
}