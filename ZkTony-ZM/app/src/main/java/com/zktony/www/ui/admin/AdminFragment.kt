package com.zktony.www.ui.admin

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.FullScreenDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnBindView
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.extension.*
import com.zktony.www.common.utils.Constants
import com.zktony.www.data.model.Version
import com.zktony.www.databinding.FragmentAdminBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class AdminFragment : BaseFragment<AdminViewModel, FragmentAdminBinding>(R.layout.fragment_admin) {

    override val viewModel: AdminViewModel by viewModels()

    @Inject
    lateinit var appViewModel: AppViewModel

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

    /**
     * 初始化Button
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun imageButtonEvent() {
        binding.reset.run {
            clickScale()
            setOnClickListener { viewModel.lowerComputerReset() }
        }
        binding.pump.addTouchEvent({
            it.scaleX = 0.8f
            it.scaleY = 0.8f
            viewModel.touchPump(true)
        }, {
            it.scaleX = 1f
            it.scaleY = 1f
            viewModel.touchPump(false)
        })
        binding.wifi.run {
            clickScale()
            setOnClickListener { viewModel.wifiSetting() }
        }
        binding.update.run {
            clickScale()
            setOnClickListener {
                if (viewModel.progress.value > 0) {
                    PopTip.show("正在更新中")
                } else {
                    viewModel.checkUpdate()
                }
            }
        }
    }

    /**
     * 初始化Switch
     */
    private fun initSwitch() {
        lifecycleScope.launch {
            delay(1200)
            binding.run {
                navigationBar.run {
                    isChecked = appViewModel.setting.value.bar
                    setOnCheckedChangeListener { _, isChecked ->
                        viewModel.toggleNavigationBar(isChecked)
                    }
                }
                audio.run {
                    isChecked = appViewModel.setting.value.audio
                    setOnCheckedChangeListener { _, isChecked ->
                        viewModel.toggleAudio(isChecked)
                    }
                }
                detect.run {
                    isChecked = appViewModel.setting.value.detect
                    setOnCheckedChangeListener { _, isChecked ->
                        viewModel.toggleDetect(isChecked)
                    }
                }
            }
        }
    }

    /**
     * 初始化编辑视图
     */
    private fun initEditView() {
        lifecycleScope.launch {
            delay(1000)
            binding.run {
                interval.run {
                    setText(appViewModel.setting.value.interval.toString())
                    afterTextChange {
                        viewModel.toggleInterval(
                            if (it.isNotEmpty()) {
                                it.toInt()
                            } else {
                                1
                            }
                        )
                    }
                }
                duration.run {
                    setText(appViewModel.setting.value.duration.toString())
                    afterTextChange {
                        viewModel.toggleDuration(
                            if (it.isNotEmpty()) {
                                it.toInt()
                            } else {
                                10
                            }
                        )
                    }
                }
                motorSpeed.run {
                    setText(appViewModel.setting.value.motorSpeed.toString())
                    afterTextChange {
                        viewModel.toggleMotorSpeed(
                            if (it.isNotEmpty()) {
                                it.toInt()
                            } else {
                                160
                            }
                        )
                    }
                }
                version.run {
                    this.clickScale()
                    this.setOnClickListener {
                        PopTip.show(R.mipmap.ic_version, "当前软件版本号 ${requireContext().versionName()}")
                    }
                }
                about.run {
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
                    setSupportZoom(false)
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
     * 初始化文本框
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
            .setMaskColor(Color.parseColor("#4D000000"))
            .show()
    }
}