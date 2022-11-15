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
import com.zktony.www.common.extension.*
import com.zktony.www.common.utils.Constants
import com.zktony.www.data.model.Version
import com.zktony.www.databinding.FragmentAdminBinding
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
            viewModel.event.collect {
                when (it) {
                    is AdminEvent.ChangeBar -> {
                        binding.sw1.isChecked = it.bar
                    }

                    is AdminEvent.ChangeAudio -> {
                        binding.sw2.isChecked = it.audio
                    }

                    is AdminEvent.ChangeDetect -> {
                        binding.sw3.isChecked = it.detect
                    }

                    is AdminEvent.ChangeInterval -> {
                        if (!binding.et1.isFocused) {
                            binding.et1.setText(it.interval.toString())
                        }
                    }

                    is AdminEvent.ChangeDuration -> {
                        if (!binding.et2.isFocused) {
                            binding.et2.setText(it.duration.toString())
                        }
                    }

                    is AdminEvent.CheckUpdate -> {
                        confirmUpdate(it.file, it.version)
                    }

                    is AdminEvent.DownloadSuccess -> {
                        binding.progress.visibility = View.INVISIBLE
                        binding.tvUpdate.run {
                            text = "检查更新"
                            setTextColor(ContextCompat.getColor(context, R.color.dark_outline))
                        }
                        requireContext().installApk(it.file)
                    }

                    is AdminEvent.DownloadError -> {
                        binding.progress.progress = 0
                        binding.progress.visibility = View.INVISIBLE
                        binding.tvUpdate.run {
                            text = "检查更新"
                            setTextColor(ContextCompat.getColor(context, R.color.dark_outline))
                        }
                        PopTip.show("下载失败,  请重试！")
                    }

                    is AdminEvent.DownloadProgress -> {
                        binding.progress.run {
                            if (visibility != View.VISIBLE) {
                                visibility = View.VISIBLE
                            }
                            if (progress != it.progress) {
                                progress = it.progress
                            }
                        }
                        binding.tvUpdate.run {
                            setTextColor(ContextCompat.getColor(context, R.color.light_primary))
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
            setOnClickListener { viewModel.rest() }
        }
        binding.ib2.addTouchEvent({
            it.scaleX = 0.8f
            it.scaleY = 0.8f
            viewModel.changePump(true)
        }, {
            it.scaleX = 1f
            it.scaleY = 1f
            viewModel.changePump(false)
        })
        binding.ib3.run {
            clickScale()
            setOnClickListener { viewModel.wifiSetting(requireContext()) }
        }
        binding.ib4.run {
            clickScale()
            setOnClickListener {
                if (viewModel.uiState.value.isUpdating) {
                    PopTip.show("正在更新中")
                } else {
                    viewModel.checkUpdate(requireContext())
                }
            }
        }
    }

    /**
     * 初始化Switch
     */
    private fun initSwitch() {
        binding.sw1.setOnCheckedChangeListener { _, isChecked ->
            viewModel.changeBar(isChecked, requireContext())
        }
        binding.sw2.setOnCheckedChangeListener { _, isChecked ->
            viewModel.changeAudio(isChecked)
        }
        binding.sw3.setOnCheckedChangeListener { _, isChecked ->
            viewModel.changeDetect(isChecked)
        }
    }

    /**
     * 初始化编辑视图
     */
    private fun initEditView() {
        binding.et1.afterTextChange {
            if (it.isNotEmpty()) {
                viewModel.changeInterval(it.removeZero().toInt())
            } else {
                viewModel.changeInterval(1)
            }
        }
        binding.et2.afterTextChange {
            if (it.isNotEmpty()) {
                viewModel.changeDuration(it.removeZero().toInt())
            } else {
                viewModel.changeDuration(10)
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
     * @param file [File] 文件
     * @param version [Version] 版本信息
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
                            viewModel.doUpdate(
                                requireContext(),
                                this@run,
                                null
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
                            viewModel.doUpdate(
                                requireContext(),
                                null,
                                this@run
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