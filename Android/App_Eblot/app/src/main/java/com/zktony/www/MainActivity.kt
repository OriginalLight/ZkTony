package com.zktony.www

import android.content.Context
import android.os.Bundle
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.zktony.core.base.BaseActivity
import com.zktony.core.ext.setLanguage
import com.zktony.core.utils.Constants
import com.zktony.datastore.ext.read
import com.zktony.www.core.ext.noticeDialog
import com.zktony.www.core.ext.serialPort
import com.zktony.www.core.ext.workerManager
import com.zktony.www.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private val datastore: DataStore<Preferences> by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        workerManager.initializer()
        serialPort.initializer()

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        binding.navView.setupWithNavController(navController)

        noticeDialog(resources.getString(R.string.notice_content))
    }

    override fun attachBaseContext(newBase: Context?) {
        var language: String
        runBlocking {
            language = datastore.read(Constants.LANGUAGE, "zh").first()
        }
        super.attachBaseContext(
            newBase?.setLanguage(language)
        )
    }
}