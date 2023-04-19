package com.zktony.www.ui.log

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import com.zktony.core.R.mipmap
import com.zktony.core.base.BaseFragment
import com.zktony.core.dialog.messageDialog
import com.zktony.core.ext.*
import com.zktony.www.R
import com.zktony.www.common.adapter.LogAdapter
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
                        empty.isVisible = it.list.isEmpty()
                        recycleView.isVisible = it.list.isNotEmpty()
                        showSearch.setIconResource(if (it.bar) mipmap.collapse_arrow else mipmap.search)
                        searchBar.isVisible = it.bar
                        startTime.text = it.startTime.simpleDateFormat("yyyy-MM-dd")
                        endTime.text = it.endTime.simpleDateFormat("yyyy-MM-dd")
                    }
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun initView() {
        adapter.onDeleteButtonClick = {
            messageDialog(
                title = getString(R.string.delete_log),
                message = "${getString(R.string.weather_delete)}？",
                block = { viewModel.delete(it) }
            )
        }
        adapter.onChartButtonClick = {
            findNavController().navigate(
                R.id.action_navigation_log_to_navigation_log_chart,
                Bundle().apply { putString("id", it.id) }
            )
        }
        binding.apply {
            recycleView.adapter = adapter
            with(showSearch) {
                clickScale()
                clickNoRepeat {
                    viewModel.showSearchBar()
                }
            }
            with(startTime) {
                clickScale()
                clickNoRepeat {
                    showDatePickerDialog(0, Calendar.getInstance(), 0)
                }
            }
            with(endTime) {
                clickScale()
                clickNoRepeat {
                    showDatePickerDialog(0, Calendar.getInstance(), 1)
                }
            }
            with(search) {
                clickScale()
                clickNoRepeat {
                    viewModel.search()
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
        calendar: Calendar,
        index: Int = 0
    ) {
        DatePickerDialog(
            requireActivity(),
            themeResId,
            { _, year, monthOfYear, dayOfMonth ->
                val dateStr = year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth
                val date = dateStr.simpleDateFormat("yyyy-MM-dd")
                date?.let { if (index == 0) viewModel.setStartTime(it) else viewModel.setEndTime(it) }
            },
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        ).show()
    }
}