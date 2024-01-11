package com.zktony.www.adapter

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.*
import com.zktony.core.ext.*
import com.zktony.www.R
import com.zktony.www.databinding.ItemLogBinding
import com.zktony.www.data.entities.LogRecord
import com.zktony.www.data.entities.Program

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 14:48
 */
class LogAdapter : ListAdapter<LogRecord, LogViewHolder>(diffCallback) {

    var selected = mutableListOf<LogRecord>()

    var callback : (List<LogRecord>) -> Unit = {}
    var onDoubleClick : (LogRecord) -> Unit = {}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        return LogViewHolder(
            ItemLogBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onItemClick = {
                if (selected.contains(it)) {
                    selected.remove(it)
                    callback(selected)
                    notifyDataSetChanged()
                } else {
                    selected.add(it)
                    callback(selected)
                    notifyDataSetChanged()
                }
            },
            onItemDoubleClick = {
                if (selected.contains(it)) {
                    selected.remove(it)
                    callback(selected)
                    notifyDataSetChanged()
                } else {
                    selected.add(it)
                    callback(selected)
                    notifyDataSetChanged()
                }
                onDoubleClick(it)
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.select(getItem(position).id in selected.map { it.id })
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
    private val onItemClick: (LogRecord) -> Unit,
    private val onItemDoubleClick: (LogRecord) -> Unit = {},
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
            itemView.onDoubleClickListener(
                onDoubleClick = {
                    onItemDoubleClick(item)
                },
                onClick = {
                    onItemClick(item)
                }
            )
            executePendingBindings()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun select(bool: Boolean) {
        binding.apply {
            if (bool) {
                order.setBackgroundColor(itemView.context.getColor(R.color.orange))
                order.setTextColor(itemView.context.getColor(R.color.white))
                experimentTime.setBackgroundColor(itemView.context.getColor(R.color.orange))
                experimentTime.setTextColor(itemView.context.getColor(R.color.white))
                model.setBackgroundColor(itemView.context.getColor(R.color.orange))
                model.setTextColor(itemView.context.getColor(R.color.white))
                parameter.setBackgroundColor(itemView.context.getColor(R.color.orange))
                parameter.setTextColor(itemView.context.getColor(R.color.white))
            } else {
                order.setBackgroundColor(itemView.context.getColor(R.color.white))
                order.setTextColor(itemView.context.getColor(R.color.black))
                experimentTime.setBackgroundColor(itemView.context.getColor(R.color.white))
                experimentTime.setTextColor(itemView.context.getColor(R.color.black))
                model.setBackgroundColor(itemView.context.getColor(R.color.white))
                model.setTextColor(itemView.context.getColor(R.color.black))
                parameter.setBackgroundColor(itemView.context.getColor(R.color.white))
                parameter.setTextColor(itemView.context.getColor(R.color.black))
            }
        }
    }
}

