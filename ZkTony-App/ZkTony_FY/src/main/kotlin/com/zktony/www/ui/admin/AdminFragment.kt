package com.zktony.www.ui.admin

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.kongzue.dialogx.dialogs.*
import com.zktony.common.base.BaseFragment
import com.zktony.common.ext.*
import com.zktony.www.BuildConfig
import com.zktony.www.R
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.extension.*
import com.zktony.www.databinding.FragmentAdminBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class AdminFragment :
    BaseFragment<AdminViewModel, FragmentAdminBinding>(R.layout.fragment_admin) {

    override val viewModel: AdminViewModel by viewModels()

    @Inject
    lateinit var appViewModel: AppViewModel

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()
    }

    /**
     * 初始化观察者
     */
    @SuppressLint("SetTextI18n")
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.file.collect {
                        it?.let {
                            updateDialog(
                                title = "发现本地新版本",
                                message = "是否更新？",
                                block = {
                                    requireContext().installApk(it)
                                    viewModel.cleanUpdate()
                                }, block1 = {
                                    viewModel.cleanUpdate()
                                })
                        }
                    }
                }
                launch {
                    viewModel.version.collect {
                        it?.let {
                            updateDialog(
                                title = "发现在线新版本",
                                message = it.description + "\n是否升级？",
                                block = {
                                    viewModel.doRemoteUpdate(it)
                                    viewModel.cleanUpdate()
                                },
                                block1 = {
                                    viewModel.cleanUpdate()
                                })
                        }
                    }
                }
                launch {
                    viewModel.progress.collect {
                        binding.apply {
                            if (it == 0) {
                                progress.visibility = View.GONE
                            } else {
                                progress.apply {
                                    visibility = View.VISIBLE
                                    progress = it
                                }
                            }
                            tvUpdate.apply {
                                if (it == 0) {
                                    text = "检查更新"
                                    setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.dark_outline
                                        )
                                    )
                                } else {
                                    text = "$it%"
                                    setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.light_primary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                launch {
                    appViewModel.settings.collect {
                        binding.etTemp.setEqualText(it.temp.toString().removeZero())
                    }
                }
            }
        }
    }

    private fun initView() {
        binding.apply {
            tvVersionName.text = BuildConfig.VERSION_NAME
            tvDeviceName.text = BuildConfig.BUILD_TYPE

            etTemp.afterTextChange {
                viewModel.setAntibodyTemp(it.toFloatOrNull() ?: 3f)
            }

            with(swBar) {
                isChecked = appViewModel.settings.value.bar
                setOnCheckedChangeListener { _, isChecked ->
                    viewModel.toggleNavigationBar(isChecked)
                }
            }
            with(setting) {
                clickScale()
                setOnClickListener {
                    authDialog {
                        selectDialog(
                            block = {
                                findNavController().navigate(R.id.action_navigation_admin_to_navigation_motor)
                            },
                            block1 = {
                                findNavController().navigate(R.id.action_navigation_admin_to_navigation_container)
                            }
                        )
                    }
                }
            }
            with(wifi) {
                clickScale()
                setOnClickListener {
                    swBar.isChecked = true
                    viewModel.toggleNavigationBar(true)
                    viewModel.wifiSetting()
                }
            }
            with(update) {
                clickScale()
                setOnClickListener {
                    if (viewModel.progress.value > 0) {
                        PopTip.show("正在更新中")
                    } else {
                        viewModel.checkUpdate()
                    }
                }
            }
            with(version) {
                clickScale()
                setOnClickListener {
                    PopTip.show(R.mipmap.ic_version, "当前软件版本号 ${BuildConfig.VERSION_NAME}")
                }
            }
            with(about) {
                clickScale()
                setOnClickListener {
                    aboutDialog()
                }
            }
            with(device) {
                clickScale()
                setOnClickListener {
                    deviceDialog()
                }
            }
        }
    }
}