package com.zktony.www.ui.log

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.*
import com.zktony.core.base.BaseFragment
import com.zktony.core.ext.*
import com.zktony.www.R
import com.zktony.www.adapter.LogAdapter
import com.zktony.www.databinding.FragmentLogBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Calendar

class LogFragment :
    BaseFragment<LogViewModel, FragmentLogBinding>(R.layout.fragment_log) {

    override val viewModel: LogViewModel by viewModel()

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
                    viewModel.uiState.collect {
                        adapter.submitList(it.list)
                        binding.apply {
                            empty.isVisible = it.list.isEmpty()
                            recycleView.isVisible = it.list.isNotEmpty()
                            showSearch.setIconResource(if (it.bar) com.zktony.core.R.mipmap.collapse_arrow else com.zktony.core.R.mipmap.search)
                            searchBar.isVisible = it.bar
                            startTime.text = it.startTime.simpleDateFormat("yyyy-MM-dd")
                            endTime.text = it.endTime.simpleDateFormat("yyyy-MM-dd")
                        }
                    }
                }
            }
        }
    }

    private fun initView() {
        adapter.onDeleteButtonClick = {
            messageDialog(
                title = getString(com.zktony.core.R.string.delete),
                message = "${getString(com.zktony.core.R.string.whether_delete)}？",
                block = { viewModel.delete(it) }
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
                    showDatePickerDialog(0, this, Calendar.getInstance(), 0)
                }
            }
            with(endTime) {
                clickScale()
                clickNoRepeat {
                    showDatePickerDialog(0, this, Calendar.getInstance(), 1)
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
        tv: TextView,
        calendar: Calendar,
        index: Int = 0
    ) {
        DatePickerDialog(
            requireActivity(),
            themeResId,
            { _, year, monthOfYear, dayOfMonth ->
                val dateStr = year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth
                val date = dateStr.simpleDateFormat("yyyy-MM-dd")
                tv.text = date?.simpleDateFormat("yyyy-MM-dd")
                date?.let { if (index == 0) viewModel.setStartTime(it) else viewModel.setEndTime(it) }
            },
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        ).show()
    }

}