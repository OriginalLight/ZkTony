package com.zktony.www.ui.log

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.zktony.www.R
import com.zktony.www.adapter.LogAdapter
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.extension.clickScale
import com.zktony.www.common.extension.getDayEnd
import com.zktony.www.common.extension.getDayStart
import com.zktony.www.common.extension.simpleDateFormat
import com.zktony.www.databinding.FragmentLogBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*


@AndroidEntryPoint
class LogFragment :
    BaseFragment<LogViewModel, FragmentLogBinding>(R.layout.fragment_log) {

    override val viewModel: LogViewModel by viewModels()

    private val logAdapter by lazy { LogAdapter() }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initObserver()
        initRecyclerView()
        initButton()
    }

    /**
     * 初始化观察者
     */
    private fun initObserver() {
        lifecycleScope.launch {
            launch {
                viewModel.logList.collect {
                    logAdapter.submitList(it)
                }
            }
            launch {
                viewModel.data.collect {
                    binding.time.text = it
                }
            }
        }
    }

    /**
     * initRecyclerView
     */
    private fun initRecyclerView() {
        binding.rc.adapter = logAdapter
        viewModel.initLogList()
    }

    /**
     * 初始化按钮
     */
    private fun initButton() {
        binding.time.run {
            clickScale()
            setOnClickListener {
                showDatePickerDialog(Calendar.getInstance())
            }
        }
    }

    /**
     * 日期选择
     * @param calendar
     */
    private fun showDatePickerDialog(calendar: Calendar) {
        DatePickerDialog(
            requireActivity(),
            0,
            { _, year, monthOfYear, dayOfMonth ->
                val dateStr = year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth
                val date = dateStr.simpleDateFormat("yyyy-MM-dd")
                date?.run { viewModel.changeLogRecord(date.getDayStart(), date.getDayEnd()) }
            },
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        ).show()
    }

}