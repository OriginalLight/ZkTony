package com.zktony.www

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.zktony.www.base.BaseActivity
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.extension.noticeDialog
import com.zktony.www.common.worker.WorkerManager
import com.zktony.www.control.motor.MotorManager
import com.zktony.www.databinding.ActivityMainBinding
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

        WorkerManager.instance.createWorker()

        noticeDialog()
    }

}