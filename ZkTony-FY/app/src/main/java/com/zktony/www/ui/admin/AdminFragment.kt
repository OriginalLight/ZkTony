package com.zktony.www.ui.admin

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.FullScreenDialog
import com.kongzue.dialogx.dialogs.InputDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnBindView
import com.kongzue.dialogx.util.InputInfo
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.constant.Constants
import com.zktony.www.common.extension.*
import com.zktony.www.data.services.model.Version
import com.zktony.www.databinding.FragmentAdminBinding
import com.zktony.www.ui.admin.model.AdminIntent
import com.zktony.www.ui.admin.model.AdminState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
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
        initEditText()
    }

    /**
     * 初始化观察者
     */
    private fun initObserver() {
        lifecycleScope.launch {
            viewModel.state.collect {
                when (it) {
                    is AdminState.ChangeBar -> changeBar()
                    is AdminState.CheckUpdate -> confirmUpdate(it.file, it.version)
                    is AdminState.DownloadSuccess -> downloadSuccess(it.file)
                    is AdminState.DownloadError -> downloadError()
                    is AdminState.DownloadProgress -> downloadProgress(it.progress)
                }
            }
        }
    }

    /**
     * 初始化按钮
     */
    private fun initButton() {
        binding.btnReset.run {
            this.clickScale()
            this.setOnClickListener { viewModel.dispatch(AdminIntent.Reset) }
        }
        binding.btnSetting.run {
            this.clickScale()
            this.setOnClickListener {
                InputDialog("权限认证", "请输入密码", "确定", "取消")
                    .setCancelable(false)
                    .setInputInfo(InputInfo().setInputType(TYPE_CLASS_TEXT or TYPE_TEXT_VARIATION_PASSWORD))
                    .setOkButton { _, _, inputStr ->
                        if (inputStr.isBlank().not() && inputStr == "123456") {
                            findNavController().navigate(R.id.action_navigation_admin_to_navigation_motor_setting)
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
                viewModel.dispatch(AdminIntent.ChangeBar(true, requireContext()))
                viewModel.dispatch(AdminIntent.WifiSetting(requireContext()))
            }
        }
        binding.btnUpdate.run {
            this.clickScale()
            this.setOnClickListener { viewModel.dispatch(AdminIntent.CheckUpdate(requireContext())) }
        }
        binding.conVersion.run {
            this.clickScale()
            this.setOnClickListener {
                PopTip.show(R.mipmap.ic_version, "当前软件版本号 ${requireContext().versionName()}")
            }
        }
        binding.conAbout.run {
            this.clickScale()
            this.setOnClickListener {
                FullScreenDialog.build(object :
                    OnBindView<FullScreenDialog>(R.layout.layout_about_webview) {
                    @SuppressLint("SetJavaScriptEnabled")
                    override fun onBind(dialog: FullScreenDialog, v: View) {
                        val btnClose = v.findViewById<View>(R.id.btn_close)
                        val webView = v.findViewById<View>(R.id.webView)
                        btnClose.setOnClickListener { dialog.dismiss() }
                        val webSettings: WebSettings = (webView as WebView).settings
                        webSettings.javaScriptEnabled = true
                        webSettings.loadWithOverviewMode = true
                        webSettings.useWideViewPort = true
                        webSettings.setSupportZoom(false)
                        webSettings.allowFileAccess = true
                        webSettings.javaScriptCanOpenWindowsAutomatically = true
                        webSettings.loadsImagesAutomatically = true
                        webSettings.defaultTextEncodingName = "utf-8"
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
        }
    }

    /**
     * 初始化开关
     */
    private fun initSwitch() {
        binding.swBar.run {
            this.isChecked = appViewModel.settingState.value.bar
            setOnCheckedChangeListener { _, isChecked ->
                viewModel.dispatch(AdminIntent.ChangeBar(isChecked, requireContext()))
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
     * 初始化输入框
     */
    private fun initEditText() {
        binding.etTemp.run {
            setText(appViewModel.settingState.value.temp.toString().removeZero())
            afterTextChange {
                if (it.isNotEmpty()) {
                    viewModel.dispatch(AdminIntent.ChangeTemp(it.toFloat()))
                }
            }
            onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus && text.toString().isEmpty()) {
                    this.setText(appViewModel.settingState.value.temp.toString().removeZero())
                }
            }
        }
    }

    /**
     * 更新导航栏开关
     */
    private fun changeBar() {
        lifecycleScope.launch {
            delay(200L)
            binding.swBar.isChecked = appViewModel.settingState.value.bar
        }
    }

    /**
     * 确认更新
     * @param file [File]
     * @param version [Version]
     */
    private fun confirmUpdate(file: File?, version: Version?) {
        file?.run {
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
                            viewModel.dispatch(
                                AdminIntent.DoUpdate(
                                    requireContext(),
                                    this@run,
                                    null
                                )
                            )
                            dialog.dismiss()
                        }
                        btnCancel.setOnClickListener { dialog.dismiss() }
                    }
                })
                .setMaskColor(Color.parseColor("#4D000000"))
                .show()
        } ?: version?.run {
            CustomDialog.build()
                .setCustomView(object : OnBindView<CustomDialog>(R.layout.layout_update_dialog) {
                    @SuppressLint("SetTextI18n")
                    override fun onBind(dialog: CustomDialog, v: View) {
                        val title = v.findViewById<TextView>(R.id.title)
                        val message = v.findViewById<TextView>(R.id.message)
                        val btnOk = v.findViewById<MaterialButton>(R.id.btn_ok)
                        val btnCancel = v.findViewById<MaterialButton>(R.id.btn_cancel)
                        title.text = "发现在线新版本"
                        message.text = this@run.description + "\n是否升级？"
                        btnOk.setOnClickListener {
                            viewModel.dispatch(
                                AdminIntent.DoUpdate(
                                    requireContext(),
                                    null,
                                    this@run
                                )
                            )
                            dialog.dismiss()
                        }
                        btnCancel.setOnClickListener { dialog.dismiss() }
                    }
                })
                .setMaskColor(Color.parseColor("#4D000000"))
                .show()
        }
    }

    /**
     * 更新成功
     * @param file [File]
     */
    private fun downloadSuccess(file: File) {
        binding.progress.visibility = View.INVISIBLE
        requireContext().installApk(file)
    }

    /**
     * 更新失败
     */
    private fun downloadError() {
        binding.progress.visibility = View.INVISIBLE
        PopTip.show("下载失败,请重试!").showLong()
    }

    /**
     * 更新进度
     * @param progress [Int]
     */
    private fun downloadProgress(progress: Int) {
        binding.progress.run {
            if (this.visibility != View.VISIBLE) {
                this.visibility = View.VISIBLE
            }
            if (this.progress != progress) {
                this.progress = progress
            }
        }
    }

}