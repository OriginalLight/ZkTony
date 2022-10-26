package com.zktony.www.ui.admin

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.FullScreenDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnBindView
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.constant.Constants
import com.zktony.www.common.extension.*
import com.zktony.www.databinding.FragmentAdminBinding
import com.zktony.www.data.services.model.Version
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class AdminFragment : BaseFragment<AdminViewModel, FragmentAdminBinding>(R.layout.fragment_admin) {

    override val viewModel: AdminViewModel by viewModels()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initObserver()
        imageButtonEvent()
        initSwitch()
        initEditView()
        initTextView()
    }

    /**
     * init观察者
     */
    @SuppressLint("SetTextI18n")
    private fun initObserver() {
        lifecycleScope.launch {
            viewModel.state.collect {
                when (it) {
                    is AdminState.ChangeBar -> {
                        binding.sw1.isChecked = it.bar
                    }

                    is AdminState.ChangeAudio -> {
                        binding.sw2.isChecked = it.audio
                    }

                    is AdminState.ChangeDetect -> {
                        binding.sw3.isChecked = it.detect
                    }

                    is AdminState.ChangeInterval -> {
                        if (!binding.et1.isFocused) {
                            binding.et1.setText(it.interval.toString())
                        }
                    }

                    is AdminState.ChangeDuration -> {
                        if (!binding.et2.isFocused) {
                            binding.et2.setText(it.duration.toString())
                        }
                    }

                    is AdminState.CheckUpdate -> {
                        confirmUpdate(it.file, it.version)
                    }

                    is AdminState.DownloadSuccess -> {
                        binding.progress.visibility = View.INVISIBLE
                        binding.tvUpdate.run {
                            text = "检查更新"
                            setTextColor(ContextCompat.getColor(context, R.color.dark_outline))
                        }
                        requireContext().installApk(it.file)
                    }

                    is AdminState.DownloadError -> {
                        binding.progress.progress = 0
                        binding.progress.visibility = View.INVISIBLE
                        "下载失败,  请重试！".showShortToast()
                    }

                    is AdminState.DownloadProgress -> {
                        binding.progress.run {
                            if (visibility != View.VISIBLE) {
                                visibility = View.VISIBLE
                            }
                            if (progress != it.progress) {
                                progress = it.progress
                            }
                        }
                        binding.tvUpdate.run {
                            setTextColor(ContextCompat.getColor(context, R.color.green))
                            text = "${it.progress}%"
                        }
                    }
                }
            }
        }
    }

    /**
     * 初始化Button
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun imageButtonEvent() {
        binding.ib1.run {
            clickScale()
            setOnClickListener { viewModel.dispatch(AdminIntent.Rest) }
        }
        binding.ib2.addTouchEvent({
            it.scaleX = 0.8f
            it.scaleY = 0.8f
            viewModel.dispatch(AdminIntent.ChangePump(true))
        }, {
            it.scaleX = 1f
            it.scaleY = 1f
            viewModel.dispatch(AdminIntent.ChangePump(false))
        })
        binding.ib3.run {
            clickScale()
            setOnClickListener { viewModel.dispatch(AdminIntent.WifiSetting(requireContext())) }
        }
        binding.ib4.run {
            clickScale()
            setOnClickListener {
                if (viewModel.uiState.value.isUpdating) {
                    PopTip.show("正在更新中")
                } else {
                    viewModel.dispatch(AdminIntent.CheckUpdate(requireContext()))
                }
            }
        }
    }

    /**
     * 初始化Switch
     */
    private fun initSwitch() {
        binding.sw1.setOnCheckedChangeListener { _, isChecked ->
            viewModel.dispatch(AdminIntent.ChangeBar(isChecked, requireContext()))
        }
        binding.sw2.setOnCheckedChangeListener { _, isChecked ->
            viewModel.dispatch(AdminIntent.ChangeAudio(isChecked))
        }
        binding.sw3.setOnCheckedChangeListener { _, isChecked ->
            viewModel.dispatch(AdminIntent.ChangeDetect(isChecked))
        }
    }

    /**
     * 初始化编辑视图
     */
    private fun initEditView() {
        binding.et1.afterTextChange {
            if (it.isNotEmpty()) {
                viewModel.dispatch(AdminIntent.ChangeInterval(it.removeZero().toInt()))
            } else {
                viewModel.dispatch(AdminIntent.ChangeInterval(1))
            }
        }
        binding.et2.afterTextChange {
            if (it.isNotEmpty()) {
                viewModel.dispatch(AdminIntent.ChangeDuration(it.removeZero().toInt()))
            } else {
                viewModel.dispatch(AdminIntent.ChangeDuration(10))
            }
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
     * 初始化文本框
     */
    private fun initTextView() {
        binding.tvVersionName.text = requireContext().versionName()
    }

    /**
     * 确认更新
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
}