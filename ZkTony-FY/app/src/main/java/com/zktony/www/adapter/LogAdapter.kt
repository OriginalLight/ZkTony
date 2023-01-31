package com.zktony.www.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zktony.www.common.extension.simpleDateFormat
import com.zktony.www.common.room.entity.Log
import com.zktony.www.databinding.ItemLogBinding

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 14:48
 */
class LogAdapter : ListAdapter<Log, LogAdapter.ViewHolder>(LogDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemLogBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemLogBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: Log) {
            binding.apply {
                log = item
                order.text = (layoutPosition + 1).toString()
                module.text = when (item.module) {
                    0 -> "A"
                    1 -> "B"
                    2 -> "C"
                    3 -> "D"
                    else -> "A"
                }
                module.setTextColor(
                    when (item.module) {
                        0 -> root.context.getColorStateList(android.R.color.holo_red_dark)
                        1 -> root.context.getColorStateList(android.R.color.holo_blue_dark)
                        2 -> root.context.getColorStateList(android.R.color.holo_green_dark)
                        3 -> root.context.getColorStateList(android.R.color.holo_orange_dark)
                        else -> root.context.getColorStateList(android.R.color.holo_red_dark)
                    }
                )
                status.text = when (item.status) {
                    0 -> "未完成"
                    1 -> "已完成"
                    else -> "未完成"
                }
                status.setTextColor(
                    when (item.status) {
                        0 -> root.context.getColorStateList(android.R.color.holo_blue_dark)
                        1 -> root.context.getColorStateList(android.R.color.holo_green_dark)
                        2 -> root.context.getColorStateList(android.R.color.holo_red_dark)
                        else -> root.context.getColorStateList(android.R.color.holo_blue_dark)
                    }
                )
                time.text = item.createTime.simpleDateFormat("yyyy-MM-dd HH:mm:ss")
                if (layoutPosition % 2 == 0) {
                    order.setBackgroundColor(Color.parseColor("#F5F5F5"))
                    module.setBackgroundColor(Color.parseColor("#F5F5F5"))
                    name.setBackgroundColor(Color.parseColor("#F5F5F5"))
                    status.setBackgroundColor(Color.parseColor("#F5F5F5"))
                    time.setBackgroundColor(Color.parseColor("#F5F5F5"))
                }

                executePendingBindings()
            }
        }
    }
}

private class LogDiffCallback : DiffUtil.ItemCallback<Log>() {

    override fun areItemsTheSame(
        oldItem: Log,
        newItem: Log
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Log,
        newItem: Log
    ): Boolean {
        return oldItem == newItem
    }
}