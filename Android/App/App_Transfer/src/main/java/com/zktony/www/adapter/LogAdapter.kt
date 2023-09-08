package com.zktony.www.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.zktony.core.ext.*
import com.zktony.www.R
import com.zktony.www.databinding.ItemLogBinding
import com.zktony.www.data.entities.LogRecord

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
            str.append(item.voltage.format())
            str.append("V - ")
            str.append(item.time.format())
            str.append("MIN")
            parameter.text = str.toString()
            val zm = itemView.context.getString(R.string.transfer)
            val rs = itemView.context.getString(R.string.dye)
            model.text = when (item.model) {
                0 -> "A-$zm"
                1 -> "A-$rs"
                2 -> "B-$zm"
                3 -> "B-$rs"
                else -> zm
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

