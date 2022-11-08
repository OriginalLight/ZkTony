package com.zktony.www.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zktony.www.R
import com.zktony.www.common.extension.removeZero
import com.zktony.www.common.extension.simpleDateFormat
import com.zktony.www.common.room.entity.Log
import com.zktony.www.databinding.ItemLogBinding

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 14:48
 */
class LogAdapter : ListAdapter<Log, LogAdapter.ViewHolder>(LogDiffCallback()) {
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
            notifyDataSetChanged()
        }
    }

    fun getItem(): Log {
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
        if (currentPosition == position && isClick) {
            holder.itemView.setBackgroundResource(R.color.dark_secondary)
        } else {
            holder.itemView.setBackgroundResource(R.color.light_onPrimary)
        }
    }


    class ViewHolder(
        private val binding: ItemLogBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: Log) {
            binding.apply {
                logRecord = item
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