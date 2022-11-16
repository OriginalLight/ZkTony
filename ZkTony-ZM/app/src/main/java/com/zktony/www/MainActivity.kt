package com.zktony.www

import android.os.Bundle
import com.zktony.www.base.BaseActivity
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.worker.WorkerManager
import com.zktony.www.databinding.ActivityMainBinding
import com.zktony.www.ui.admin.AdminFragment
import com.zktony.www.ui.home.HomeFragment
import com.zktony.www.ui.log.LogFragment
import com.zktony.www.ui.program.ProgramFragment
import dagger.hilt.android.AndroidEntryPoint
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
    lateinit var appViewModel: AppViewModel

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
        WorkerManager.instance.createWorker()
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
}