package com.zktony.www.ui.log

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.kongzue.dialogx.dialogs.MessageDialog
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

    private val adapter by lazy { LogAdapter() }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initTextView()
        initRecyclerView()
    }


    private fun initFlowCollector() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.logList.collect {
                    adapter.submitList(it)
                    if (it.isEmpty()) {
                        binding.empty.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    } else {
                        binding.empty.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    /**
     * initRecyclerView
     */
    private fun initRecyclerView() {
        binding.recyclerView.adapter = adapter
        adapter.setOnDeleteButtonClick {
            MessageDialog.show(
                "提示",
                "确定删除该日志吗？",
                "确定",
                "取消"
            ).setOkButton { _, _ ->
                viewModel.delete(it)
                false
            }
        }
        adapter.setOnChartButtonClick {
            val direction =
                LogFragmentDirections.actionNavigationLogToNavigationLogChart(it.id)
            findNavController().navigate(direction)
        }
        viewModel.initLogRecord()
    }

    @SuppressLint("SimpleDateFormat")
    private fun initTextView() {
        binding.datePicker.run {
            clickScale()
            text = Date(System.currentTimeMillis()).simpleDateFormat("MM 月 dd 日")
            setOnClickListener {
                showDatePickerDialog(0, binding.datePicker, Calendar.getInstance())
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