package com.zktony.www.ui.admin

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.gson.Gson
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.R.color
import com.zktony.common.base.BaseFragment
import com.zktony.common.dialog.aboutDialog
import com.zktony.common.dialog.deviceDialog
import com.zktony.common.dialog.updateDialog
import com.zktony.common.ext.*
import com.zktony.www.BuildConfig
import com.zktony.www.R
import com.zktony.www.data.remote.model.QrCode
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

    /**
     * init观察者
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
                    viewModel.application.collect {
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
                                            color.dark_outline
                                        )
                                    )
                                } else {
                                    text = "$it%"
                                    setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            color.light_primary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                launch {
                    stateManager.setting.collect {
                        binding.apply {
                            if (it.interval > 0) interval.setEqualText(
                                it.interval.toString().removeZero()
                            )
                            if (it.duration > 0) duration.setEqualText(
                                it.duration.toString().removeZero()
                            )
                            if (it.motorSpeed > 0) motorSpeed.setEqualText(
                                it.motorSpeed.toString().removeZero()
                            )
                            navigationBar.isChecked = it.bar
                            audio.isChecked = it.audio
                            detect.isChecked = it.detect
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

            interval.afterTextChange {
                viewModel.toggleInterval(it.toIntOrNull() ?: 0)
            }

            duration.afterTextChange {
                viewModel.toggleDuration(it.toIntOrNull() ?: 0)
            }

            motorSpeed.afterTextChange {
                viewModel.toggleMotorSpeed(it.toIntOrNull() ?: 0)
            }

            with(navigationBar) {
                isChecked = stateManager.setting.value.bar
                setOnCheckedChangeListener { _, isChecked ->
                    viewModel.toggleNavigationBar(isChecked)
                }
            }

            with(audio) {
                isChecked = stateManager.setting.value.audio
                setOnCheckedChangeListener { _, isChecked ->
                    viewModel.toggleAudio(isChecked)
                }
            }

            with(detect) {
                isChecked = stateManager.setting.value.detect
                setOnCheckedChangeListener { _, isChecked ->
                    viewModel.toggleDetect(isChecked)
                }
            }

            with(reset) {
                clickScale()
                clickNoRepeat { viewModel.lowerComputerReset() }
            }

            with(wifi) {
                clickScale()
                clickNoRepeat { viewModel.wifiSetting() }
            }

            with(update) {
                clickScale()
                clickNoRepeat {
                    if (viewModel.progress.value > 0) {
                        PopTip.show("正在更新中")
                    } else {
                        viewModel.checkUpdate()
                    }
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
                    val id = Settings.Secure.getString(
                        Ext.ctx.contentResolver, Settings.Secure.ANDROID_ID
                    )
                    deviceDialog(
                        Gson().toJson(
                            QrCode(
                                id = id,
                            )
                        )
                    )
                }
            }
        }
    }
}