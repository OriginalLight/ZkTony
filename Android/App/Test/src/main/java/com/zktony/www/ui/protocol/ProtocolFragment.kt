package com.zktony.www.ui.protocol

import android.os.Bundle
import androidx.lifecycle.*
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.getTimeFormat
import com.zktony.www.R
import com.zktony.www.databinding.FragmentProtocolBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProtocolFragment :
    BaseFragment<ProtocolViewModel, FragmentProtocolBinding>(R.layout.fragment_protocol) {

    override val viewModel: ProtocolViewModel by viewModel()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()
    }

    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect {
                    binding.apply {
                        s1.text = it.map[0]!!.toString()
                        s2.text = it.map[1]!!.toString()
                        s3.text = it.map[2]!!.toString()
                        s4.text = it.map[3]!!.toString()
                        s5.text = it.map[4]!!.toString()
                        s6.text = it.map[5]!!.toString()
                        s7.text = it.map[6]!!.toString()
                        s8.text = it.map[7]!!.toString()
                        s9.text = it.map[8]!!.toString()
                        s10.text = it.map[9]!!.toString()
                        s11.text = it.map[10]!!.toString()
                        s12.text = it.map[11]!!.toString()
                        s13.text = it.map[12]!!.toString()
                        s14.text = it.map[13]!!.toString()
                        s15.text = it.map[14]!!.toString()
                        s16.text = it.map[15]!!.toString()
                        sync.isEnabled = it.asyncJob == null
                        async.isEnabled = it.syncJob == null
                        time.text = it.time.getTimeFormat()
                        sendNum.text = it.sendNum.toString()
                        receiveNum.text = it.receiveNum.toString()
                        sendText.text = it.sendText
                        receiveText.text = it.receiveText
                    }
                } }
            }
        }
    }

    private fun initView() {
        binding.apply {
            sync.setOnClickListener {
                viewModel.sync()
            }
            async.setOnClickListener {
                viewModel.async()
            }
        }
    }
}