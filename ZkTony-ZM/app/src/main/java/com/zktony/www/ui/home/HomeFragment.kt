package com.zktony.www.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.tabs.TabLayout
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.extension.clickScale
import com.zktony.www.common.extension.removeZero
import com.zktony.www.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>(R.layout.fragment_home) {

    override val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var appViewModel: AppViewModel

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initTabLayout()
        initButton()
    }

    /**
     * 初始化Flow收集器
     */
    @SuppressLint("SetTextI18n")
    private fun initFlowCollector() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiStateX.collect {
                        if (it.model == 0) {
                            binding.x.run {
                                tabLayout.getTabAt(0)?.select()
                                pump.visibility = View.VISIBLE
                                motor.isEnabled = true
                            }
                        } else {
                            binding.x.run {
                                tabLayout.getTabAt(1)?.select()
                                pump.visibility = View.GONE
                                motor.isEnabled = false
                            }
                        }
                        binding.x.run {
                            programName.text = it.programName
                            if (it.model == 0) {
                                if(it.motor > 0) {
                                    motor.setText(it.motor.toString() + " RPM")
                                } else {
                                    motor.setText("")
                                }
                            } else {
                                motor.setText("/")
                            }
                            if(it.voltage > 0f) {
                                voltage.setText(it.voltage.toString().removeZero() + " V")
                            } else {
                                voltage.setText("")
                            }
                            if(it.time > 0) {
                                time.setText(it.time.toString().removeZero() + " MIN")
                            } else {
                                time.setText("")
                            }
                        }
                    }
                }
                launch {
                    viewModel.uiStateY.collect {
                        if (it.model == 0) {
                            binding.y.run {
                                tabLayout.getTabAt(0)?.select()
                                pump.visibility = View.VISIBLE
                                motor.isEnabled = true
                            }
                        } else {
                            binding.y.run {
                                tabLayout.getTabAt(1)?.select()
                                pump.visibility = View.GONE
                                motor.isEnabled = false
                            }
                        }
                        binding.y.run {
                            programName.text = it.programName
                            if (it.model == 0) {
                                if(it.motor > 0) {
                                    motor.setText(it.motor.toString() + " RPM")
                                } else {
                                    motor.setText("")
                                }
                            } else {
                                motor.setText("/")
                            }
                            if(it.voltage > 0f) {
                                voltage.setText(it.voltage.toString().removeZero() + " V")
                            } else {
                                voltage.setText("")
                            }
                            if(it.time > 0) {
                                time.setText(it.time.toString().removeZero() + " MIN")
                            } else {
                                time.setText("")
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 初始化TabLayout
     */
    private fun initTabLayout() {
        binding.x.tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewModel.setModel(tab?.position ?: 0, 0)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
        binding.y.tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewModel.setModel(tab?.position ?: 0, 1)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    /**
     * 初始化按钮
     */
    private fun initButton() {
        binding.x.run {
            selector.run {
                clickScale()
                setOnClickListener {

                }
            }
        }
        binding.y.run {
            selector.run {
                clickScale()
                setOnClickListener {

                }
            }
        }
    }

}