package com.zktony.www

import android.os.Bundle
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.zktony.serialport.COMSerial
import com.zktony.serialport.listener.OnComDataListener
import com.zktony.www.base.BaseActivity
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.app.AppIntent
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.audio.AudioPlayer
import com.zktony.www.common.constant.Constants
import com.zktony.www.common.model.Event
import com.zktony.www.common.model.SerialPort.TTYS4
import com.zktony.www.databinding.ActivityMainBinding
import com.zktony.www.ui.admin.AdminFragment
import com.zktony.www.ui.home.HomeFragment
import com.zktony.www.ui.home.model.Cmd
import com.zktony.www.ui.log.LogFragment
import com.zktony.www.ui.program.ProgramFragment
import com.zktony.www.worker.WorkerManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    companion object {
        private const val KEY_CURRENT_FRAGMENT_INDEX = "key_current_fragment_index"
    }

    private var fragmentList =
        listOf(
            HomeFragment(),
            ProgramFragment(),
            LogFragment(),
            AdminFragment()
        )
    private var activeFragmentIndex = -1

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    @Inject
    lateinit var appViewModel: AppViewModel
    private var audio = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            initFragment()
        }
        binding.navView.setNavigationItemSelectedListener {
            switchFragment(getFragmentIndexFromItemId(it.itemId))
            it.isChecked = true
            true
        }
        initView()
    }

    private fun initView() {
        EventBus.getDefault().register(this)
        WorkerManager.instance.createWorker()
        initSerialPort()
        initSettings()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        fragmentList = fragmentList.map {
            supportFragmentManager.findFragmentByTag(it.javaClass.simpleName) as? BaseFragment<*, *>
                ?: it
        }
        switchFragment(savedInstanceState.getInt(KEY_CURRENT_FRAGMENT_INDEX, 0))
    }

    private fun switchFragment(fragmentIndex: Int) {
        binding.navView.menu.findItem(R.id.navigation_home).isChecked = false
        binding.navView.menu.findItem(R.id.navigation_program).isChecked = false
        binding.navView.menu.findItem(R.id.navigation_log).isChecked = false
        binding.navView.menu.findItem(R.id.navigation_admin).isChecked = false
        if (fragmentIndex != activeFragmentIndex) {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            val fragment = fragmentList[fragmentIndex]
            fragmentList.getOrNull(activeFragmentIndex)?.apply(fragmentTransaction::hide)
            if (!fragment.isAdded) {
                fragmentTransaction
                    .add(
                        R.id.nav_host_fragment_content_main,
                        fragment,
                        fragment.javaClass.simpleName
                    )
                    .show(fragment)
            } else {
                fragmentTransaction.show(fragment)
            }
            fragmentTransaction.commitAllowingStateLoss()
            activeFragmentIndex = fragmentIndex
        }
    }

    private fun getFragmentIndexFromItemId(itemId: Int): Int {
        return when (itemId) {
            R.id.navigation_home -> 0
            R.id.navigation_program -> 1
            R.id.navigation_log -> 2
            R.id.navigation_admin -> 3
            else -> 0
        }
    }

    /**
     * 初始化Fragment
     */
    private fun initFragment() {
        fragmentList.forEach {
            if (!it.isAdded && it !is HomeFragment) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.nav_host_fragment_content_main, it, it.javaClass.simpleName)
                    .hide(it)
                    .commitAllowingStateLoss()
            }
        }
        switchFragment(0)
        binding.navView.menu.findItem(R.id.navigation_home).isChecked = true
    }

    /**
     * 初始化设置
     */
    private fun initSettings() {
        lifecycleScope.launch {
            dataStore.data.map { preferences ->
                preferences[booleanPreferencesKey(Constants.AUDIO)] ?: true
            }.distinctUntilChanged().collect {
                audio = it
            }
        }
    }

    /**
     * 初始化串口
     */
    private fun initSerialPort() {
        COMSerial.instance.addCOM(TTYS4.device, 115200)
        COMSerial.instance.addDataListener(object : OnComDataListener {
            override fun comDataBack(com: String, hexData: String) {
                val cmd = Cmd(hexData)
                if (cmd.cmd == 2) {
                    appViewModel.dispatch(AppIntent.ReceiveCmd(cmd))
                }
            }
        })
    }

    /**
     * 使用EventBus来线程间通信
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGetMessage(event: Event<String, Int>) {
        if (Constants.AUDIOID == event.type && audio) {
            lifecycleScope.launch {
                AudioPlayer.instance.play(this@MainActivity, event.message)
            }
        }
    }

}