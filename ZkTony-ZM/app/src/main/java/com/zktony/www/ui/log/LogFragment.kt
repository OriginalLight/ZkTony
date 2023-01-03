package com.zktony.www.ui.log

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
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
import com.zktony.www.common.extension.simpleDateFormat
import com.zktony.www.data.model.LogData
import com.zktony.www.data.model.LogRecord
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
    private var logRecordList = emptyList<LogRecord>()
    private var logDataList = emptyList<LogData>()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initTextView()
        initObserver()
        initRecyclerView()
        buttonEvent()
    }

    /**
     * 初始化Observer
     */
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
                        refreshButton()
                    }

                    is LogEvent.ChangeLogData -> {
                        logDataList = it.logDataList
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
        logAdapter.setOnClick { refreshButton() }
        viewModel.initLogRecord()
    }

    @SuppressLint("SimpleDateFormat")
    private fun initTextView() {
        binding.tv1.text = Date(System.currentTimeMillis()).simpleDateFormat("MM 月 dd 日")
        binding.tv1.setOnClickListener {
            showDatePickerDialog(0, binding.tv1, Calendar.getInstance())
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

    /**
     * 按钮事件
     */
    private fun buttonEvent() {
        binding.ib1.run {
            this.clickScale()
            this.setOnClickListener {
                binding.lc1.visibility = View.GONE
                binding.lc2.visibility = View.GONE
                binding.con2.visibility = View.GONE
                binding.con1.visibility = View.VISIBLE
            }
        }
        binding.btn1.setOnClickListener {
            binding.con1.visibility = View.GONE
            binding.con2.visibility = View.VISIBLE
            binding.lc1.visibility = View.VISIBLE
            binding.lc2.visibility = View.GONE
            binding.tvChart.text = "电压图"
            initVoltageChart()
        }
        binding.btn2.setOnClickListener {
            binding.con1.visibility = View.GONE
            binding.con2.visibility = View.VISIBLE
            binding.lc1.visibility = View.GONE
            binding.lc2.visibility = View.VISIBLE
            binding.tvChart.text = "电流图"
            initCurrentChart()
        }
    }

    /**
     * init折线图
     */
    private fun initVoltageChart() {
        if (logDataList.isNotEmpty()) {
            val entries: MutableList<Entry> = ArrayList()
            val maxVol = (logDataList[0].voltage + 0.5f).toInt() + 2f
            val minVol = (logDataList[0].voltage + 0.5f).toInt() - 2f
            logDataList.forEachIndexed { index, logData ->
                entries.add(Entry((index * 5 + 5).toFloat(), logData.voltage))
            }
            val dataSet = LineDataSet(entries, "电压 V")
            dataSet.color = Color.parseColor("#ff5500") //线条颜色
            dataSet.setDrawCircles(false)
            dataSet.setDrawValues(false)
            dataSet.lineWidth = 1f //线条宽度
            val chart = binding.lc1
            val description = Description()
            description.text = "单位 秒"
            description.textSize = 11f
            chart.description = description
            val lineData = LineData(dataSet)
            initXY(chart, maxVol, minVol)
            chart.data = lineData
            chart.invalidate()
            chart.animateXY(500, 500)
        }
    }

    /**
     * init折线图
     */
    private fun initCurrentChart() {
        if (logDataList.isNotEmpty()) {
            val entries: MutableList<Entry> = ArrayList()
            var maxCurrent = 0f
            val minCurrent = 0f
            logDataList.forEachIndexed { index, logData ->
                entries.add(Entry((index * 5 + 5).toFloat(), logData.current))
                if (logData.current > maxCurrent) {
                    maxCurrent = logData.current
                }
            }
            maxCurrent = maxCurrent.toInt() + 1f
            val dataSet = LineDataSet(entries, "电流 A")
            dataSet.color = Color.parseColor("#ff5500") //线条颜色
            dataSet.setDrawCircles(false)
            dataSet.setDrawValues(false)
            dataSet.lineWidth = 1f //线条宽度
            val chart = binding.lc2
            val description = Description()
            description.text = "单位 秒"
            description.textSize = 11f
            chart.description = description
            val lineData = LineData(dataSet)
            initXY(chart, maxCurrent, minCurrent)
            chart.data = lineData
            chart.invalidate()
            chart.animateXY(500, 500)
        }
    }

    /**
     * init xy
     *
     * @param chart 图表
     * @param max   最大值
     * @param min   最小值
     */
    private fun initXY(chart: LineChart, max: Float, min: Float) {
        chart.clear()
        //设置样式
        val rightAxis = chart.axisRight
        //设置图表右边的y轴禁用
        rightAxis.isEnabled = false
        val leftAxis = chart.axisLeft
        //设置图表左边的y轴禁用
        leftAxis.isEnabled = true
        leftAxis.textColor = Color.parseColor("#333333")
        leftAxis.enableGridDashedLine(0f, 0f, 0f)
        leftAxis.axisMaximum = max
        leftAxis.axisMinimum = min


        //设置x轴
        val xAxis = chart.xAxis
        xAxis.textColor = Color.parseColor("#333333")
        xAxis.textSize = 11f
        xAxis.axisMinimum = 0f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
    }

    private fun refreshButton() {
        if (logRecordList.isEmpty() || logAdapter.currentPosition == -1 || !logAdapter.isClick) {
            binding.btn1.visibility = View.GONE
            binding.btn2.visibility = View.GONE
        } else {
            binding.btn1.visibility = View.VISIBLE
            binding.btn2.visibility = View.VISIBLE
            viewModel.changeLogData(logAdapter.getItem().id)
        }
    }
}