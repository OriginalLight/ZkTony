package com.zktony.www

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.zktony.www.base.BaseActivity
import com.zktony.www.common.app.AppEvent
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.extension.hexToAscii
import com.zktony.www.common.extension.verifyHex
import com.zktony.www.common.worker.WorkerManager
import com.zktony.www.databinding.ActivityMainBinding
import com.zktony.www.serialport.SerialPortEnum
import com.zktony.www.serialport.SerialPortManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    @Inject
    lateinit var appViewModel: AppViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        binding.navView.setupWithNavController(navController)

        initView()
    }

    private fun initView() {
        WorkerManager.instance.createWorker()
        lifecycleScope.launch {
            launch {
                SerialPortManager.instance.addDataListener { com, hexData ->
                    when (com) {
                        SerialPortEnum.SERIAL_ONE.device -> {
                            hexData.verifyHex().forEach {
                                appViewModel.receiverSerialOne(it)
                            }
                        }
                        SerialPortEnum.SERIAL_TWO.device -> {
                            hexData.verifyHex().forEach {
                                appViewModel.receiverSerialTwo(it)
                            }
                        }
                        SerialPortEnum.SERIAL_THREE.device -> {
                            hexData.verifyHex().forEach {
                                appViewModel.receiverSerialThree(it)
                            }
                        }
                        SerialPortEnum.SERIAL_FOUR.device -> {
                            appViewModel.receiverSerialFour(hexData.hexToAscii())
                        }
                    }
                }
            }
            launch {
                appViewModel.event.collect {
                    when (it) {
                        is AppEvent.Sender -> {
                            SerialPortManager.instance.sendHex(it.serialPort, it.command)
                        }
                        is AppEvent.SenderText -> {
                            SerialPortManager.instance.sendText(it.serialPort, it.command)
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}