package com.zktony.www.ui.admin

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.core.view.isVisible
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.kongzue.dialogx.dialogs.*
import com.kongzue.dialogx.interfaces.OnBindView
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.*
import com.zktony.core.model.QrCode
import com.zktony.core.utils.Constants
import com.zktony.datastore.ext.read
import com.zktony.www.BuildConfig
import com.zktony.www.R
import com.zktony.www.common.ext.*
import com.zktony.www.databinding.FragmentAdminBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class AdminFragment :
    BaseFragment<AdminViewModel, FragmentAdminBinding>(R.layout.fragment_admin) {

    override val viewModel: AdminViewModel by viewModel()

    private val dataStore: DataStore<Preferences> by inject()

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
                    viewModel.uiState.collect {
                        binding.apply {
                            progress.apply {
                                progress = it.progress
                                isVisible = it.progress != 0
                            }
                            tvUpdate.text =
                                if (it.progress == 0) resources.getString(com.zktony.core.R.string.check_update) else "${it.progress}%"
                            it.application?.let { app ->
                                if (app.versionCode > BuildConfig.VERSION_CODE) {
                                    update.setBackgroundResource(com.zktony.core.R.mipmap.new_icon)
                                    tvUpdate.text =
                                        if (it.progress == 0) resources.getString(com.zktony.core.R.string.new_version) else "${it.progress}%"
                                }
                            }
                        }
                    }
                }
                launch {
                    launch {
                        dataStore.read(Constants.TEMP, 5.0f).collect {
                            binding.etTemp.setEqualText(it.toString().removeZero())
                        }
                    }
                    launch {
                        dataStore.read(Constants.BAR, true).collect {
                            binding.swBar.isChecked = it
                        }
                    }
                    launch {
                        dataStore.read(Constants.RECYCLE, true).collect {
                            binding.swRecycle.isChecked = it
                        }
                    }
                    launch {
                        dataStore.read(Constants.LANGUAGE, "zh").collect {
                            binding.btnLanguage.text = when (it) {
                                "zh" -> "简体中文"
                                "en" -> "English"
                                else -> "简体中文"
                            }
                        }
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

            etTemp.afterTextChange {
                viewModel.setAntibodyTemp(it.toFloatOrNull() ?: 3f)
            }

            btnLanguage.clickNoRepeat {
                spannerDialog(
                    it,
                    menu = listOf(
                        "简体中文", "English"
                    ),
                    block = { _, index -> viewModel.setLanguage(index) }
                )
            }

            swBar.setOnCheckedChangeListener { _, isChecked ->
                viewModel.toggleNavigationBar(isChecked)
            }
            swRecycle.setOnCheckedChangeListener { _, isChecked ->
                viewModel.toggleRecycle(isChecked)
            }
            with(setting) {
                clickScale()
                clickNoRepeat {
                    authDialog {
                        CustomDialog.build()
                            .setCustomView(object :
                                OnBindView<CustomDialog>(R.layout.layout_select) {
                                override fun onBind(dialog: CustomDialog, v: View) {
                                    val motor = v.findViewById<MaterialButton>(R.id.motor)
                                    val container = v.findViewById<MaterialButton>(R.id.container)
                                    motor.clickNoRepeat(1000L) {
                                        findNavController().navigate(R.id.action_navigation_admin_to_navigation_motor)
                                        dialog.dismiss()
                                    }
                                    container.clickNoRepeat(1000L) {
                                        findNavController().navigate(R.id.action_navigation_admin_to_navigation_container)
                                        dialog.dismiss()
                                    }
                                }
                            })
                            .setMaskColor(Color.parseColor("#4D000000"))
                            .show()
                    }
                }
            }
            with(wifi) {
                clickScale()
                clickNoRepeat {
                    swBar.isChecked = true
                    viewModel.toggleNavigationBar(true)
                    viewModel.wifiSetting()
                }
            }
            with(update) {
                clickScale()
                clickNoRepeat {
                    if (viewModel.uiState.value.progress > 0) {
                        PopTip.show(resources.getString(com.zktony.core.R.string.updating))
                        return@clickNoRepeat
                    }
                    if (viewModel.uiState.value.loading) {
                        PopTip.show(resources.getString(com.zktony.core.R.string.checking_for_updates))
                        return@clickNoRepeat
                    }
                    viewModel.checkUpdate()
                }
            }
            with(version) {
                clickScale()
                clickNoRepeat {
                    PopTip.show("${resources.getString(com.zktony.core.R.string.version)} ${BuildConfig.VERSION_NAME}")
                }
            }
            with(about) {
                clickScale()
                clickNoRepeat {
                    aboutDialog { webDialog() }
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