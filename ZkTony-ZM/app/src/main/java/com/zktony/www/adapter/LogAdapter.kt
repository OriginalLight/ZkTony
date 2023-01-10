package com.zktony.www.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zktony.www.common.extension.clickScale
import com.zktony.www.common.extension.removeZero
import com.zktony.www.common.extension.simpleDateFormat
import com.zktony.www.data.model.LogRecord
import com.zktony.www.databinding.ItemLogBinding

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 14:48
 */
class LogAdapter : ListAdapter<LogRecord, LogAdapter.ViewHolder>(LogDiffCallback()) {

    private lateinit var onDeleteButtonClick: (LogRecord) -> Unit
    private lateinit var onChartButtonClick: (LogRecord) -> Unit


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemLogBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onDeleteButtonClick,
            onChartButtonClick
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setOnDeleteButtonClick(onDeleteButtonClick: (LogRecord) -> Unit) {
        this.onDeleteButtonClick = onDeleteButtonClick
    }

    fun setOnChartButtonClick(onChartButtonClick: (LogRecord) -> Unit) {
        this.onChartButtonClick = onChartButtonClick
    }


    @SuppressLint("SetTextI18n")
    class ViewHolder(
        private val binding: ItemLogBinding,
        private val onDeleteButtonClick: (LogRecord) -> Unit,
        private val onChartButtonClick: (LogRecord) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LogRecord) {
            binding.apply {
                order.text = (layoutPosition + 1).toString()
                experimentTime.text = item.createTime.simpleDateFormat("MM-dd HH:mm")
                val str = StringBuilder()
                if (item.model == 0 || item.model == 2) {
                    str.append(item.motor)
                    str.append("RPM - ")
                }
                str.append(item.voltage.toString().removeZero())
                str.append("V - ")
                str.append(item.time.toString().removeZero())
                str.append("MIN")
                parameter.text = str.toString()
                model.text = when (item.model) {
                    0 -> "A-转膜"
                    1 -> "A-染色"
                    2 -> "B-转膜"
                    3 -> "B-染色"
                    else -> "未知"
                }
                delete.run {
                    this.clickScale()
                    setOnClickListener {
                        onDeleteButtonClick(item)
                    }
                }
                chart.run {
                    this.clickScale()
                    setOnClickListener {
                        onChartButtonClick(item)
                    }
                }
                if (layoutPosition % 2 == 0) {
                    order.setBackgroundColor(Color.parseColor("#F5F5F5"))
                    experimentTime.setBackgroundColor(Color.parseColor("#F5F5F5"))
                    model.setBackgroundColor(Color.parseColor("#F5F5F5"))
                    parameter.setBackgroundColor(Color.parseColor("#F5F5F5"))
                }

                executePendingBindings()
            }
        }
    }
}

private class LogDiffCallback : DiffUtil.ItemCallback<LogRecord>() {

    override fun areItemsTheSame(
        oldItem: LogRecord,
        newItem: LogRecord
    ): Boolean {
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(
        oldItem: LogRecord,
        newItem: LogRecord
    ): Boolean {
        return oldItem == newItem
    }
}