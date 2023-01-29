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

        viewModel.init()

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 5
            }

            override fun createFragment(position: Int): Fragment {
                val fragmentOne = PlateFragment()
                val fragmentTwo = PlateFragment()
                val fragmentThree = PlateFragment()
                val fragmentFour = PlateFragment()
                fragmentOne.arguments = Bundle().apply {
                    putInt("position", position)
                }
                fragmentTwo.arguments = Bundle().apply {
                    putInt("position", position)
                }
                fragmentThree.arguments = Bundle().apply {
                    putInt("position", position)
                }
                fragmentFour.arguments = Bundle().apply {
                    putInt("position", position)
                }
                return when (position) {
                    0 -> {
                        fragmentOne
                    }
                    1 -> {
                        fragmentTwo
                    }
                    2 -> {
                        fragmentThree
                    }
                    3 -> {
                        fragmentFour
                    }
                    4 -> {
                        WashFragment()
                    }
                    else -> {
                        fragmentOne
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