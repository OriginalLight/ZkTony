package com.zktony.www

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.zktony.www.base.BaseActivity
import com.zktony.www.common.app.AppIntent
import com.zktony.www.common.app.AppState
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.extension.verifyHex
import com.zktony.www.databinding.ActivityMainBinding
import com.zktony.www.model.enum.SerialPortEnum
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
        // WorkerManager.instance.createWorker()
        lifecycleScope.launch {
            SerialPortManager.instance.addDataListener { com, hexData ->
                when (com) {
                    SerialPortEnum.SERIAL_ONE.device -> {
                        hexData.verifyHex().forEach {
                            appViewModel.dispatch(AppIntent.ReceiverSerialOne(it))
                        }
                    }
                    SerialPortEnum.SERIAL_TWO.device -> {
                        hexData.verifyHex().forEach {
                            appViewModel.dispatch(AppIntent.ReceiverSerialTwo(it))
                        }
                    }
                    SerialPortEnum.SERIAL_THREE.device -> {
                        hexData.verifyHex().forEach {
                            appViewModel.dispatch(AppIntent.ReceiverSerialThree(it))
                        }
                    }
                    SerialPortEnum.SERIAL_FOUR.device -> {
                        hexData.verifyHex().forEach {
                            appViewModel.dispatch(AppIntent.ReceiverSerialFour(it))
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            appViewModel.state.collect {
                when (it) {
                    is AppState.Sender -> {
                        SerialPortManager.instance.sendHex(it.serialPort, it.command)
                    }
                    is AppState.SenderText -> {
                        SerialPortManager.instance.sendText(it.serialPort, it.command)
                    }
                    else -> {}
                }
            }
        }
    }
}