package com.zktony.www.ui.admin

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.kongzue.dialogx.dialogs.*
import com.zktony.core.base.BaseFragment
import com.zktony.core.dialog.aboutDialog
import com.zktony.core.dialog.authDialog
import com.zktony.core.dialog.deviceDialog
import com.zktony.core.dialog.updateDialog
import com.zktony.core.ext.Ext
import com.zktony.core.ext.clickNoRepeat
import com.zktony.core.ext.clickScale
import com.zktony.core.ext.installApk
import com.zktony.www.BuildConfig
import com.zktony.www.R
import com.zktony.www.common.ext.*
import com.zktony.core.model.QrCode
import com.zktony.www.databinding.FragmentAdminBinding
import com.zktony.www.manager.StateManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class AdminFragment : BaseFragment<AdminViewModel, FragmentAdminBinding>(R.layout.fragment_admin) {

    override val viewModel: AdminViewModel by viewModel()

    private val stateManager: StateManager by inject()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()
    }

    @SuppressLint("SetTextI18n")
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect {
                        if (it.file != null) {
                            updateDialog(
                                title = "发现本地新版本",
                                message = "是否更新？",
                                block = {
                                    requireContext().installApk(it.file)
                                    viewModel.cleanUpdate()
                                }, block1 = {
                                    viewModel.cleanUpdate()
                                })
                        }
                        if (it.application != null) {
                            updateDialog(
                                title = "发现在线新版本",
                                message = it.application.description + "\n是否升级？",
                                block = {
                                    viewModel.doRemoteUpdate(it.application)
                                    viewModel.cleanUpdate()
                                },
                                block1 = {
                                    viewModel.cleanUpdate()
                                })
                        }
                        binding.apply {
                            progress.apply {
                                progress = it.progress
                                isVisible = it.progress != 0
                            }
                            tvUpdate.text = if (it.progress == 0) "检查更新" else "${it.progress}%"
                        }
                    }
                }
                launch {
                    stateManager.settings.collect {
                        binding.swBar.isChecked = it.bar
                    }
                }
            }
        }
    }

    @SuppressLint("HardwareIds")
    private fun initView() {
        binding.apply {
            tvVersionName.text = BuildConfig.VERSION_NAME
            tvDeviceName.text = BuildConfig.BUILD_TYPE

            swBar.setOnCheckedChangeListener { _, isChecked ->
                viewModel.toggleNavigationBar(isChecked)
            }
            with(setting) {
                clickScale()
                clickNoRepeat {
                    authDialog { findNavController().navigate(R.id.action_navigation_admin_to_navigation_motor) }
                }
            }
            with(wifi) {
                clickScale()
                clickNoRepeat {
                    viewModel.wifiSetting()
                }
            }
            with(update) {
                clickScale()
                clickNoRepeat {
                    if (viewModel.uiState.value.progress > 0) {
                        PopTip.show("正在更新中")
                        return@clickNoRepeat
                    }
                    if (viewModel.uiState.value.loading) {
                        PopTip.show("正在检查更新中")
                        return@clickNoRepeat
                    }
                    viewModel.checkUpdate()
                }
            }
            with(version) {
                clickScale()
                clickNoRepeat {
                    PopTip.show("当前软件版本号 ${BuildConfig.VERSION_NAME}")
                }
            }
            with(about) {
                clickScale()
                clickNoRepeat {
                    aboutDialog()
                }
            }
            with(device) {
                clickScale()
                clickNoRepeat {
                    deviceDialog(
                        Gson().toJson(
                            QrCode(
                                id = Settings.Secure.getString(
                                    Ext.ctx.contentResolver, Settings.Secure.ANDROID_ID
                                ),
                                `package` = BuildConfig.APPLICATION_ID,
                                version_name = BuildConfig.VERSION_NAME,
                                version_code = BuildConfig.VERSION_CODE,
                                build_type = BuildConfig.BUILD_TYPE,
                            )
                        )
                    )
                }
            }
        }
    }
}