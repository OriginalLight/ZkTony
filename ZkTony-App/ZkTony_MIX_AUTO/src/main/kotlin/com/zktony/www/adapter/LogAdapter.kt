package com.zktony.www.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zktony.common.R
import com.zktony.common.ext.clickScale
import com.zktony.common.ext.simpleDateFormat
import com.zktony.www.data.local.room.entity.Log
import com.zktony.www.databinding.ItemLogBinding

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 14:48
 */
class LogAdapter : ListAdapter<Log, LogAdapter.ViewHolder>(LogDiffCallback()) {

    private var onDeleteButtonClick: (Log) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemLogBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onDeleteButtonClick
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setOnDeleteButtonClick(onDeleteButtonClick: (Log) -> Unit) {
        this.onDeleteButtonClick = onDeleteButtonClick
    }

    class ViewHolder(
        private val binding: ItemLogBinding,
        private val onDeleteButtonClick: (Log) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: Log) {
            binding.apply {
                log = item
                order.text = (layoutPosition + 1).toString()
                name.text = item.workName
                time.text = item.createTime.simpleDateFormat("yyyy-MM-dd HH:mm:ss")
                status.text = when (item.status) {
                    0 -> "未完成"
                    1 -> "完成"
                    else -> "未完成"
                }
                status.setTextColor(
                    when (item.status) {
                        0 -> R.color.red
                        1 -> R.color.green
                        else -> R.color.red
                    }
                )
                text.text = item.content
                with(delete) {
                    clickScale()
                    setOnClickListener {
                        onDeleteButtonClick(item)
                    }
                }
                with(spacer) {
                    clickScale()
                    setOnClickListener {
                        if (content.visibility == View.GONE) {
                            content.visibility = View.VISIBLE
                            ivSpacer.setImageResource(R.mipmap.collapse_arrow)
                        } else {
                            content.visibility = View.GONE
                            ivSpacer.setImageResource(R.mipmap.expand_arrow)
                        }
                    }
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