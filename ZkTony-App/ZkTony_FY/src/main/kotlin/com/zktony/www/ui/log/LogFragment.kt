package com.zktony.www.ui.log

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.zktony.common.base.BaseFragment
import com.zktony.common.extension.clickScale
import com.zktony.common.extension.getDayEnd
import com.zktony.common.extension.getDayStart
import com.zktony.common.extension.simpleDateFormat
import com.zktony.www.R
import com.zktony.www.adapter.LogAdapter
import com.zktony.www.databinding.FragmentLogBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*


@AndroidEntryPoint
class LogFragment :
    BaseFragment<LogViewModel, FragmentLogBinding>(R.layout.fragment_log) {

    override val viewModel: LogViewModel by viewModels()

    private val adapter by lazy { LogAdapter() }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()
    }

    /**
     * 初始化观察者
     */
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.logList.collect {
                        adapter.submitList(it)
                        binding.apply {
                            recycleView.isVisible = it.isNotEmpty()
                            empty.isVisible = it.isEmpty()
                        }
                    }
                }
                launch {
                    viewModel.data.collect {
                        binding.time.text = it
                    }
                }
            }
        }
    }

    private fun initView() {
        binding.apply {
            recycleView.adapter = adapter

            with(time) {
                clickScale()
                setOnClickListener {
                    showDatePickerDialog(Calendar.getInstance())
                }
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