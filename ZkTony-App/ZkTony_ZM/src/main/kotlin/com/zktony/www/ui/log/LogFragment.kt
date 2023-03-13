package com.zktony.www.ui.log

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.zktony.common.base.BaseFragment
import com.zktony.common.dialog.deleteDialog
import com.zktony.common.ext.clickScale
import com.zktony.common.ext.getDayEnd
import com.zktony.common.ext.getDayStart
import com.zktony.common.ext.simpleDateFormat
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


    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.logList.collect {
                    adapter.submitList(it)
                    binding.apply {
                        empty.isVisible = it.isEmpty()
                        recyclerView.isVisible = it.isNotEmpty()
                    }
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun initView() {
        adapter.setOnDeleteButtonClick {
            deleteDialog(name = "该日志", block = { viewModel.delete(it) })
        }
        adapter.setOnChartButtonClick {
            findNavController().navigate(
                R.id.action_navigation_log_to_navigation_log_chart,
                Bundle().apply { putString("id", it.id) }
            )
        }
        binding.apply {
            recyclerView.adapter = adapter
            with(datePicker) {
                clickScale()
                text = Date(System.currentTimeMillis()).simpleDateFormat("MM 月 dd 日")
                setOnClickListener {
                    showDatePickerDialog(0, binding.datePicker, Calendar.getInstance())
                }
            }
        }
    }

    /**
     * 日期选择
     * @param themeResId
     * @param tv
     * @param calendar
     */
    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    fun showDatePickerDialog(
        themeResId: Int,
        tv: TextView,
        calendar: Calendar
    ) {
        DatePickerDialog(
            requireActivity(),
            themeResId,
            { _, year, monthOfYear, dayOfMonth ->
                val dateStr = year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth
                val date = dateStr.simpleDateFormat("yyyy-MM-dd")
                tv.text = date?.simpleDateFormat("MM 月 dd 日")
                date?.run { viewModel.changeLogRecord(date.getDayStart(), date.getDayEnd()) }
            },
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        ).show()
    }
}