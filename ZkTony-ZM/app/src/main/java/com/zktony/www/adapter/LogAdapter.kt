package com.zktony.www.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zktony.www.R
import com.zktony.www.common.constant.Constants
import com.zktony.www.common.extension.removeZero
import com.zktony.www.common.extension.simpleDateFormat
import com.zktony.www.common.model.Event
import com.zktony.www.data.entity.LogRecord
import com.zktony.www.databinding.ItemLogBinding
import org.greenrobot.eventbus.EventBus

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 14:48
 */
class LogAdapter : ListAdapter<LogRecord, LogAdapter.ViewHolder>(LogDiffCallback()) {
    var isClick = false
    var currentPosition = -1

    private fun setCurrentPosition(isClick: Boolean, position: Int) {
        this.isClick = isClick
        this.currentPosition = position
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun itemClickEvent(holder: ViewHolder) {
        holder.itemView.setOnClickListener {
            if (!isClick) {
                setCurrentPosition(true, holder.bindingAdapterPosition)
            } else {
                setCurrentPosition(
                    currentPosition != holder.bindingAdapterPosition,
                    holder.bindingAdapterPosition
                )
            }
            EventBus.getDefault().post(Event(Constants.BLANK, Constants.LOG_CLICK))
            notifyDataSetChanged()
        }
    }

    fun getItem(): LogRecord {
        return getItem(currentPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = ViewHolder(
            ItemLogBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
        itemClickEvent(holder)
        return holder
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
        if (currentPosition == position && isClick) {
            holder.itemView.setBackgroundResource(R.color.dark_secondary)
        } else {
            holder.itemView.setBackgroundResource(R.color.light_onPrimary)
        }
    }


    @SuppressLint("SetTextI18n")
    class ViewHolder(
        private val binding: ItemLogBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LogRecord) {
            binding.apply {
                logRecord = item
                tv1.text = (layoutPosition + 1).toString()
                tv2.text = item.createTime.simpleDateFormat("MM-dd HH:mm")
                val str = StringBuilder()
                if (item.model == 0) {
                    str.append(item.motor)
                    str.append("R/MIN-")
                }
                str.append(item.voltage.toString().removeZero())
                str.append("V-")
                str.append((item.time / 60).toString().removeZero())
                str.append("MIN")
                tv4.text = str.toString()
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