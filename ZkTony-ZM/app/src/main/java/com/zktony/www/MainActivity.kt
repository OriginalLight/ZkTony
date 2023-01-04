package com.zktony.www

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.interfaces.OnBindView
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

    @Inject
    lateinit var appViewModel: AppViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        binding.navView.setupWithNavController(navController)

        WorkerManager.instance.createWorker()
        notice()
    }

    /**
     * 显示注意事项
     */
    private fun notice() {
        CustomDialog.build()
            .setCustomView(object : OnBindView<CustomDialog>(R.layout.layout_notice_dialog) {
                override fun onBind(dialog: CustomDialog, v: View) {
                    val btnOk = v.findViewById<MaterialButton>(R.id.btn_ok)
                    btnOk.setOnClickListener {
                        dialog.dismiss()
                    }
                }
            }).setMaskColor(Color.parseColor("#4D000000")).setWidth(500).show()
    }
}