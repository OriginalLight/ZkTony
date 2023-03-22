package com.zktony.www

import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.zktony.common.base.BaseActivity
import com.zktony.common.dialog.noticeDialog
import com.zktony.www.databinding.ActivityMainBinding
import com.zktony.www.manager.StateManager
import org.koin.android.ext.android.inject

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private val stateManager: StateManager by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navigationView = binding.navView
        navigationView.itemIconTintList = null

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        binding.navView.setupWithNavController(navController)

        stateManager.initApp()

        noticeDialog(resources.getString(R.string.notice_content))
    }

}