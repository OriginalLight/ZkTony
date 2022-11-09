package com.zktony.www.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zktony.www.common.extension.simpleDateFormat
import com.zktony.www.common.room.entity.Log
import com.zktony.www.databinding.ItemLogBinding
import com.zktony.www.ui.home.getModuleFromIndex

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
                module.text = getModuleFromIndex(item.module).value
                time.text = item.createTime.simpleDateFormat("yyyy-MM-dd HH:mm:ss")
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