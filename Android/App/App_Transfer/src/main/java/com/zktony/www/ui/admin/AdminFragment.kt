package com.zktony.www.ui.admin

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import androidx.core.view.isVisible
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.*
import com.google.gson.Gson
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.*
import com.zktony.core.model.QrCode
import com.zktony.core.utils.Constants
import com.zktony.core.ext.read
import com.zktony.www.BuildConfig
import com.zktony.www.R
import com.zktony.www.databinding.FragmentAdminBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class AdminFragment : BaseFragment<AdminViewModel, FragmentAdminBinding>(R.layout.fragment_admin) {

    override val viewModel: AdminViewModel by viewModel()

    private val dataStore: DataStore<Preferences> by inject()


    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()
    }

    /**
     * init观察者
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
                        dataStore.read(Constants.AUDIO, true).collect {
                            binding.swAudio.isChecked = it
                        }
                    }
                    launch {
                        dataStore.read(Constants.BAR, false).collect {
                            binding.swBar.isChecked = it
                        }
                    }
                    launch {
                        dataStore.read(Constants.DETECT, true).collect {
                            binding.swDetect.isChecked = it
                        }
                    }
                    launch {
                        dataStore.read(Constants.INTERVAL, 1).collect {
                            if (it > 0) binding.interval.setEqualText(it.toString())
                        }
                    }
                    launch {
                        dataStore.read(Constants.DURATION, 10).collect {
                            if (it > 0) binding.duration.setEqualText(it.toString())
                        }
                    }
                    launch {
                        dataStore.read(Constants.MOTOR_SPEED, 160).collect {
                            if (it > 0) binding.motorSpeed.setEqualText(it.toString())
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

    /**
     * 初始化Button
     */
    @SuppressLint("ClickableViewAccessibility", "HardwareIds")
    private fun initView() {
        binding.apply {

            intervalModule.isVisible = BuildConfig.BUILD_TYPE != "beta"
            durationModule.isVisible = BuildConfig.BUILD_TYPE != "beta"
            motorModule.isVisible = BuildConfig.BUILD_TYPE != "beta"
            pumpModule.isVisible = BuildConfig.BUILD_TYPE != "beta"

            tvVersionName.text = BuildConfig.VERSION_NAME
            tvDeviceName.text = BuildConfig.BUILD_TYPE

            pump.addTouchEvent({
                it.scaleX = 0.8f
                it.scaleY = 0.8f
                viewModel.touchPump(true)
            }, {
                it.scaleX = 1f
                it.scaleY = 1f
                viewModel.touchPump(false)
            })

            btnLanguage.clickNoRepeat {
                spannerDialog(
                    it,
                    menu = listOf(
                        "简体中文", "English"
                    ),
                    block = { _, index -> viewModel.setLanguage(index) }
                )
            }

            interval.afterTextChange {
                viewModel.toggleInterval(it.toIntOrNull() ?: 0)
            }

            duration.afterTextChange {
                viewModel.toggleDuration(it.toIntOrNull() ?: 0)
            }

            motorSpeed.afterTextChange {
                viewModel.toggleMotorSpeed(it.toIntOrNull() ?: 0)
            }

            swBar.setOnCheckedChangeListener { _, isChecked ->
                viewModel.toggleNavigationBar(isChecked)
            }

            swAudio.setOnCheckedChangeListener { _, isChecked ->
                viewModel.toggleAudio(isChecked)
            }

            swDetect.setOnCheckedChangeListener { _, isChecked ->
                viewModel.toggleDetect(isChecked)
            }

            with(wifi) {
                clickScale()
                clickNoRepeat { viewModel.wifiSetting() }
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