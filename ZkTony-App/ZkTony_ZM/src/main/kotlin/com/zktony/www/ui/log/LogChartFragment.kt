package com.zktony.www.ui.log

import android.graphics.Color
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.zktony.common.base.BaseFragment
import com.zktony.common.ext.clickNoRepeat
import com.zktony.common.ext.clickScale
import com.zktony.www.R
import com.zktony.www.data.local.room.entity.LogData
import com.zktony.www.databinding.FragmentLogChartBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class LogChartFragment :
    BaseFragment<LogChartViewModel, FragmentLogChartBinding>(R.layout.fragment_log_chart) {

    override val viewModel: LogChartViewModel by viewModel()
    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()
    }

    /**
     * 初始化Flow收集器
     */
    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.logList.collect {
                    initData(it)
                }
            }
        }
    }

    /**
     * initChart
     */
    private fun initView() {
        arguments?.let {
            val id = it.getString("id") ?: ""
            if (id.isNotEmpty()) {
                viewModel.loadData(id)
            }
        }
        binding.back.run {
            clickScale()
            clickNoRepeat {
                findNavController().navigateUp()
            }
        }
        val chart = binding.lineChart
        chart.description = Description().apply {
            text = "X轴为时间，左Y轴为电压，右Y轴为电流"
            textSize = 11f
            textColor = Color.BLACK
        }
        chart.setNoDataText("暂无数据")
        chart.setNoDataTextColor(Color.BLACK)
        initXY(chart)

    }

    /**
     * 初始化图表数据
     */
    private fun initData(logData: List<LogData>) {
        val chart = binding.lineChart
        if (logData.isEmpty()) {
            chart.clear()
            return
        }
        val voltageList: MutableList<Entry> = ArrayList()
        val currentList: MutableList<Entry> = ArrayList()
        logData.forEachIndexed { index, log ->
            voltageList.add(Entry((index * 5 + 5).toFloat(), log.voltage))
            currentList.add(Entry((index * 5 + 5).toFloat(), log.current * 10f))
        }
        if (chart.data != null && chart.data.dataSetCount > 0) {
            val voltageDataSet = chart.data.getDataSetByIndex(0) as LineDataSet
            val currentDataSet = chart.data.getDataSetByIndex(1) as LineDataSet
            voltageDataSet.values = voltageList
            currentDataSet.values = currentList
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()
        } else {
            val voltageSet = LineDataSet(voltageList, "电压 V")
            val currentSet = LineDataSet(currentList, "电流 A")
            voltageSet.color = Color.parseColor("#3A50D0") //线条颜色
            voltageSet.setDrawCircles(false)
            voltageSet.setDrawValues(false)
            voltageSet.lineWidth = 1f //线条宽度
            currentSet.color = Color.parseColor("#FF0000") //线条颜色
            currentSet.setDrawCircles(false)
            currentSet.setDrawValues(false)
            currentSet.lineWidth = 1f //线条宽度
            chart.data = LineData(voltageSet, currentSet)
            chart.invalidate()
            chart.animateXY(500, 500)
        }
    }

    /**
     * init xy
     *
     * @param chart 图表
     */
    private fun initXY(chart: LineChart) {
        chart.clear()
        //设置样式
        val rightAxis = chart.axisRight
        val leftAxis = chart.axisLeft
        //设置图表左边的y轴
        leftAxis.isEnabled = true
        leftAxis.textColor = Color.parseColor("#3A50D0")
        leftAxis.enableGridDashedLine(5f, 5f, 0f)
        leftAxis.axisLineColor = Color.parseColor("#3A50D0")
        leftAxis.axisMaximum = 80f
        leftAxis.axisMinimum = 0f
        //设置图表右边的y轴
        rightAxis.isEnabled = true
        rightAxis.textColor = Color.parseColor("#FF0000")
        rightAxis.enableGridDashedLine(5f, 5f, 0f)
        rightAxis.axisLineColor = Color.parseColor("#FF0000")
        rightAxis.axisMaximum = 8f
        rightAxis.axisMinimum = 0f

        //设置x轴
        val xAxis = chart.xAxis
        xAxis.textColor = Color.parseColor("#333333")
        xAxis.enableGridDashedLine(5f, 5f, 0f)
        xAxis.textSize = 11f
        xAxis.axisMinimum = 0f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
    }
}