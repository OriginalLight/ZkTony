package com.zktony.www

import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.zktony.core.base.BaseActivity
import com.zktony.www.core.ext.noticeDialog
import com.zktony.www.core.ext.serialPort
import com.zktony.www.core.ext.workerManager
import com.zktony.www.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        workerManager.initializer()
        serialPort.initializer()

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        binding.navView.setupWithNavController(navController)

        noticeDialog(resources.getString(R.string.notice_content))
    }
}