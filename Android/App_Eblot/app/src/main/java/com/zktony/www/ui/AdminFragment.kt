package com.zktony.www.ui

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
import com.zktony.core.utils.Constants
import com.zktony.datastore.ext.read
import com.zktony.www.BuildConfig
import com.zktony.www.R
import com.zktony.www.core.ext.aboutDialog
import com.zktony.www.core.ext.spannerDialog
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
    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect {
                        binding.apply {
                            tvUpdate.text =
                                if (it.progress == 0) resources.getString(R.string.check_update) else "${it.progress}%"
                            if (it.job == null) {
                                pump.background = resources.getDrawable(R.drawable.bg_img_btn_unpressed)
                            } else {
                                pump.background = resources.getDrawable(R.drawable.bg_img_btn_pressed)
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
    @SuppressLint("ClickableViewAccessibility", "HardwareIds", "UseCompatLoadingForDrawables")
    private fun initView() {
        binding.apply {
            tvVersionName.text = BuildConfig.VERSION_NAME

            pump.clickNoRepeat {
                 viewModel.touchPump()
            }

            motorSpeed.afterTextChange {
                viewModel.toggleMotorSpeed(it.toIntOrNull() ?: 0)
            }

            swBar.setOnCheckedChangeListener { _, isChecked ->
                viewModel.toggleNavigationBar(isChecked)
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
                        PopTip.show(resources.getString(R.string.updating))
                        return@clickNoRepeat
                    }
                    if (viewModel.uiState.value.loading) {
                        PopTip.show(resources.getString(R.string.checking_for_updates))
                        return@clickNoRepeat
                    }
                    viewModel.checkUpdate()
                }
            }

            with(version) {
                clickScale()
                clickNoRepeat {
                    PopTip.show("${resources.getString(R.string.version)} ${BuildConfig.VERSION_NAME}")
                }
            }
        }
    }
}