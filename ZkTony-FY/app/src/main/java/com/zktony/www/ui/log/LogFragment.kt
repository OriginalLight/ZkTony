package com.zktony.www.ui.log

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.zktony.www.R
import com.zktony.www.adapter.LogAdapter
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.extension.clickScale
import com.zktony.www.common.extension.getDayEnd
import com.zktony.www.common.extension.getDayStart
import com.zktony.www.common.room.entity.Log
import com.zktony.www.databinding.FragmentLogBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.*


@AndroidEntryPoint
class LogFragment :
    BaseFragment<LogViewModel, FragmentLogBinding>(R.layout.fragment_log) {

    override val viewModel: LogViewModel by viewModels()

    private val logAdapter by lazy { LogAdapter() }
    private var logRecordList = emptyList<Log>()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initObserver()
        initRecyclerView()
        buttonEvent()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObserver() {
        lifecycleScope.launch {
            viewModel.event.distinctUntilChanged().collect {
                when (it) {
                    is LogEvent.ChangeLogRecord -> {
                        logAdapter.submitList(it.logRecordList)
                        logRecordList = it.logRecordList
                        logAdapter.currentPosition = -1
                        logAdapter.isClick = false
                    }
                }
            }
        }
    }

    /**
     * initRecyclerView
     */
    private fun initRecyclerView() {
        binding.rc1.adapter = logAdapter
        viewModel.changeLogRecord(
            Date(System.currentTimeMillis()).getDayStart(),
            Date(System.currentTimeMillis()).getDayEnd()
        )
    }


    /**
     * 按钮事件
     */
    private fun buttonEvent() {
        binding.ib1.run {
            this.clickScale()
        }
    }

}