package com.zktony.www.ui.admin

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.*
import com.kongzue.dialogx.interfaces.OnBindView
import com.kongzue.dialogx.util.InputInfo
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.extension.*
import com.zktony.www.common.utils.Constants
import com.zktony.www.data.model.Version
import com.zktony.www.databinding.FragmentAdminBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class AdminFragment :
    BaseFragment<AdminViewModel, FragmentAdminBinding>(R.layout.fragment_admin) {

    override val viewModel: AdminViewModel by viewModels()

    @Inject
    lateinit var appViewModel: AppViewModel

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initObserver()
        initButton()
        initSwitch()
        initTextView()
    }

    /**
     * 初始化观察者
     */
    @SuppressLint("SetTextI18n")
    private fun initObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.file.collect {
                        it?.let { showLocalUpdate(it) }
                    }
                }
                launch {
                    viewModel.version.collect {
                        it?.let { showRemoteUpdate(it) }
                    }
                }
                launch {
                    viewModel.progress.collect { progress ->
                        binding.progress.run {
                            if (progress == 0) {
                                visibility = View.GONE
                            } else {
                                visibility = View.VISIBLE
                                setProgress(progress)
                            }
                        }
                        binding.tvUpdate.run {
                            if (progress == 0) {
                                text = "检查更新"
                                setTextColor(ContextCompat.getColor(context, R.color.dark_outline))
                            } else {
                                text = "$progress%"
                                setTextColor(ContextCompat.getColor(context, R.color.light_primary))
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 初始化按钮
     */
    private fun initButton() {
        binding.btnSetting.run {
            this.clickScale()
            this.setOnClickListener {
                InputDialog("权限认证", "请输入密码", "确定", "取消")
                    .setCancelable(false)
                    .setInputInfo(InputInfo().setInputType(TYPE_CLASS_TEXT or TYPE_TEXT_VARIATION_PASSWORD))
                    .setOkButton { _, _, inputStr ->
                        if (inputStr.isBlank().not() && inputStr == "123456") {
                            CustomDialog.build()
                                .setCustomView(object :
                                    OnBindView<CustomDialog>(R.layout.layout_function_select) {
                                    override fun onBind(dialog: CustomDialog, v: View) {
                                        val motor = v.findViewById<MaterialButton>(R.id.motor)
                                        val cancel = v.findViewById<MaterialButton>(R.id.cancel)
                                        motor.setOnClickListener {
                                            if (isFastClick().not()) {
                                                dialog.dismiss()
                                                findNavController().navigate(R.id.action_navigation_admin_to_navigation_motor)
                                            }
                                        }
                                        cancel.setOnClickListener { dialog.dismiss() }
                                    }
                                })
                                .setMaskColor(Color.parseColor("#4D000000"))
                                .show()
                        } else {
                            PopTip.show("密码错误")
                        }
                        false
                    }
                    .show()
            }
        }
        binding.btnWifi.run {
            this.clickScale()
            this.setOnClickListener {
                binding.swBar.isChecked = true
                viewModel.toggleNavigationBar(true)
                viewModel.wifiSetting()
            }
        }
        binding.btnUpdate.run {
            this.clickScale()
            this.setOnClickListener {
                if (viewModel.progress.value > 0) {
                    PopTip.show("正在更新中")
                } else {
                    viewModel.checkUpdate()
                }
            }
        }
        binding.version.run {
            this.clickScale()
            this.setOnClickListener {
                PopTip.show(R.mipmap.ic_version, "当前软件版本号 ${requireContext().versionName()}")
            }
        }
        binding.about.run {
            this.clickScale()
            this.setOnClickListener {
                CustomDialog.build()
                    .setCustomView(object : OnBindView<CustomDialog>(R.layout.layout_about_dialog) {
                        override fun onBind(dialog: CustomDialog, v: View) {
                            val btnWeb = v.findViewById<MaterialButton>(R.id.btn_web)
                            btnWeb.isVisible = requireContext().isNetworkAvailable()
                            btnWeb.setOnClickListener {
                                if (isFastClick().not()) {
                                    dialog.dismiss()
                                    toWebSite()
                                }
                            }
                        }
                    })
                    .setMaskColor(Color.parseColor("#4D000000"))
                    .show()
            }
        }
    }

    /**
     * 打开公司网站
     */
    private fun toWebSite() {
        FullScreenDialog.build(object :
            OnBindView<FullScreenDialog>(R.layout.layout_about_webview) {
            @SuppressLint("SetJavaScriptEnabled")
            override fun onBind(dialog: FullScreenDialog, v: View) {
                val btnClose = v.findViewById<View>(R.id.btn_close)
                val webView = v.findViewById<View>(R.id.webView)
                btnClose.setOnClickListener { dialog.dismiss() }
                (webView as WebView).settings.apply {
                    javaScriptEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    setSupportZoom(true)
                    allowFileAccess = true
                    javaScriptCanOpenWindowsAutomatically = true
                    loadsImagesAutomatically = true
                    defaultTextEncodingName = "utf-8"
                }
                webView.webViewClient = object : WebViewClient() {
                    @Deprecated("Deprecated in Java")
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        url: String
                    ): Boolean {
                        view.loadUrl(url)
                        return true
                    }

                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)
                    }
                }
                webView.loadUrl(Constants.DOMAIN)
            }
        }).setMaxWidth(1920).show()
    }

    /**
     * 初始化开关
     */
    private fun initSwitch() {
        binding.swBar.run {
            this.isChecked = appViewModel.settings.value.bar
            setOnCheckedChangeListener { _, isChecked ->
                viewModel.toggleNavigationBar(isChecked)
            }
        }
    }

    /**
     * 初始化文本
     */
    private fun initTextView() {
        binding.tvVersionName.text = requireContext().versionName()
    }

    /**
     * 显示本地更新
     * @param file [File]
     */
    private fun showLocalUpdate(file: File) {
        CustomDialog.build()
            .setCustomView(object : OnBindView<CustomDialog>(R.layout.layout_update_dialog) {
                override fun onBind(dialog: CustomDialog, v: View) {
                    val title = v.findViewById<TextView>(R.id.title)
                    val message = v.findViewById<TextView>(R.id.message)
                    val btnOk = v.findViewById<MaterialButton>(R.id.btn_ok)
                    val btnCancel = v.findViewById<MaterialButton>(R.id.btn_cancel)
                    title.text = "发现本地新版本"
                    message.text = "是否更新？"
                    btnOk.setOnClickListener {
                        requireContext().installApk(file)
                        viewModel.cleanUpdate()
                        dialog.dismiss()
                    }
                    btnCancel.setOnClickListener {
                        viewModel.cleanUpdate()
                        dialog.dismiss()
                    }
                }
            })
            .setCancelable(false)
            .setMaskColor(Color.parseColor("#4D000000"))
            .show()
    }

    /**
     * 显示更新
     * @param version [Version]
     */
    private fun showRemoteUpdate(version: Version) {
        CustomDialog.build()
            .setCustomView(object : OnBindView<CustomDialog>(R.layout.layout_update_dialog) {
                @SuppressLint("SetTextI18n")
                override fun onBind(dialog: CustomDialog, v: View) {
                    val title = v.findViewById<TextView>(R.id.title)
                    val message = v.findViewById<TextView>(R.id.message)
                    val btnOk = v.findViewById<MaterialButton>(R.id.btn_ok)
                    val btnCancel = v.findViewById<MaterialButton>(R.id.btn_cancel)
                    title.text = "发现在线新版本"
                    message.text = version.description + "\n是否升级？"
                    btnOk.setOnClickListener {
                        viewModel.doRemoteUpdate(version)
                        viewModel.cleanUpdate()
                        dialog.dismiss()
                    }
                    btnCancel.setOnClickListener {
                        viewModel.cleanUpdate()
                        dialog.dismiss()
                    }
                }
            })
            .setCancelable(false)
            .setMaskColor(Color.parseColor("#4D000000"))
            .show()
    }
}