package com.zktony.www.ui

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.*
import com.zktony.www.R
import com.zktony.www.adapter.LogAdapter
import com.zktony.www.core.ext.messageDialog
import com.zktony.www.databinding.FragmentLogBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class LogFragment :
    BaseFragment<LogViewModel, FragmentLogBinding>(R.layout.fragment_log) {

    override val viewModel: LogViewModel by viewModel()

    private val adapter by lazy { LogAdapter() }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    adapter.submitList(it.list)
                    binding.apply {
                        lineChart.isEnabled = it.selected != null && adapter.selected != null
                        delete.isEnabled = it.selected != null && adapter.selected != null
                    }
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun initView() {
        binding.apply {
            recycleView.adapter = adapter
            adapter.callback =  { viewModel.select(it) }
            lineChart.clickNoRepeat {
                findNavController().navigate(
                    R.id.action_navigation_log_to_navigation_log_chart,
                    Bundle().apply { putString("id", adapter.selected!!.id) }
                )
            }
            datePicker.clickNoRepeat {
                showDatePickerDialog(0, Calendar.getInstance())
            }
            delete.clickNoRepeat {
                messageDialog(
                    title = getString(R.string.delete_log),
                    message = "您确定要删除该日志吗？",
                    block = {
                        viewModel.delete(adapter.selected!!)
                        adapter.selected = null
                        viewModel.select(null)
                    },
                )
            }
        }
    }

    /**
     * 日期选择
     * @param themeResId
     * @param calendar
     */
    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    fun showDatePickerDialog(
        themeResId: Int,
        calendar: Calendar,
    ) {
        DatePickerDialog(
            requireActivity(),
            themeResId,
            { _, year, monthOfYear, dayOfMonth ->
                val dateStr = year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth
                val date = dateStr.simpleDateFormat("yyyy-MM-dd")
                date?.let { viewModel.search(it) }
            },
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        ).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.select(null)
    }
}