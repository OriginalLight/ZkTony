package com.zktony.www.common.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zktony.common.ext.clickNoRepeat
import com.zktony.common.ext.clickScale
import com.zktony.common.ext.removeZero
import com.zktony.common.ext.simpleDateFormat
import com.zktony.www.data.local.room.entity.LogRecord
import com.zktony.www.databinding.ItemLogBinding

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 14:48
 */
class LogAdapter : ListAdapter<LogRecord, LogViewHolder>(diffCallback) {

    var onDeleteButtonClick: (LogRecord) -> Unit = {}
    var onChartButtonClick: (LogRecord) -> Unit = {}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        return LogViewHolder(
            ItemLogBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onDeleteButtonClick,
            onChartButtonClick
        )
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<LogRecord>() {
            override fun areItemsTheSame(oldItem: LogRecord, newItem: LogRecord): Boolean {
                return oldItem.id == newItem.id
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: LogRecord, newItem: LogRecord): Boolean {
                return oldItem == newItem
            }
        }
    }
}

@SuppressLint("SetTextI18n")
class LogViewHolder(
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
                clickNoRepeat {
                    onDeleteButtonClick(item)
                }
            }
            chart.run {
                this.clickScale()
                clickNoRepeat {
                    onChartButtonClick(item)
                }
            }
            executePendingBindings()
        }
    }
}

