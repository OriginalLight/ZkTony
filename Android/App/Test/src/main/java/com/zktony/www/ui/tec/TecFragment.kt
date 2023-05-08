package com.zktony.www.ui.tec

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import androidx.lifecycle.*
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.format
import com.zktony.www.R
import com.zktony.www.databinding.FragmentTecBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class TecFragment : BaseFragment<TecViewModel, FragmentTecBinding>(R.layout.fragment_tec) {

    override val viewModel: TecViewModel by viewModel()


    override fun onViewCreated(savedInstanceState: Bundle?) {
        viewModel.init()
        initFlowCollector()
        for (i in 0..4) {
            initButton(i)
        }
    }

    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState0.collect { uiChange(it, 0) } }
                launch { viewModel.uiState1.collect { uiChange(it, 1) } }
                launch { viewModel.uiState2.collect { uiChange(it, 2) } }
                launch { viewModel.uiState3.collect { uiChange(it, 3) } }
                launch { viewModel.uiState4.collect { uiChange(it, 4) } }
            }
        }
    }

    private fun initButton(flag: Int) {
        val bind = bind(flag)
        bind.run {
            start.setOnClickListener {
                if (bind.start.text == "开始") {
                    viewModel.start(flag)
                } else {
                    viewModel.stop(flag)
                }
            }
            count.setOnClickListener {
                viewModel.showError(flag)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun uiChange(uiState: TecUiState, flag: Int) {
        val bind = bind(flag)
        bind.title.text = uiState.title
        bind.setTemp.text = "${uiState.setTemp.format()} ℃"
        bind.temp.text = "${uiState.temp.format()} ℃"
        bind.count.run {
            text = uiState.count.toString()
            if (uiState.error != null) {
                setTextColor(Color.parseColor("#FF0000"))
            } else {
                setTextColor(Color.parseColor("#6200EA"))
            }
        }
        if (uiState.job != null) {
            bind.start.text = "停止"
        } else {
            bind.start.text = "开始"
        }
    }

    private fun bind(flag: Int) = when (flag) {
        0 -> binding.a
        1 -> binding.b
        2 -> binding.c
        3 -> binding.d
        4 -> binding.e
        else -> binding.a
    }
}