package com.zktony.www.common.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.R
import com.zktony.common.ext.clickScale
import com.zktony.common.ext.removeZero
import com.zktony.www.data.local.room.entity.Action
import com.zktony.www.data.local.room.entity.getActionEnum
import com.zktony.www.databinding.ItemActionBinding

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 11:27
 */
class ActionAdapter : ListAdapter<Action, ActionAdapter.ViewHolder>(ActionDiffCallback()) {

    private lateinit var onDeleteButtonClick: (Action) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemActionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), onDeleteButtonClick
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setOnDeleteButtonClick(onDeleteButtonClick: (Action) -> Unit) {
        this.onDeleteButtonClick = onDeleteButtonClick
    }

    class ViewHolder(
        private val binding: ItemActionBinding, private val onDeleteButtonClick: (Action) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: Action) {
            binding.apply {
                action = item
                when (item.mode) {
                    0 -> {
                        icon.setBackgroundResource(R.mipmap.box)
                        con4.visibility = View.GONE
                    }
                    3 -> {
                        icon.setBackgroundResource(R.mipmap.clean)
                        con4.visibility = View.VISIBLE
                    }
                    else -> {
                        icon.setBackgroundResource(R.mipmap.virus)
                        con4.visibility = View.GONE
                    }
                }
                model.text = getActionEnum(item.mode).value
                time.text = item.time.removeZero() + if (item.mode == 3) " 分钟" else " 小时"
                temperature.text = item.temperature.removeZero() + " ℃"
                water.text = item.liquidVolume.removeZero() + " 微升"
                counter.text = item.count.toString() + " 次"
                cardView.setOnClickListener {
                    PopTip.show("点击右侧图标删除")
                }
                with(delete) {
                    clickScale()
                    setOnClickListener {
                        onDeleteButtonClick.invoke(item)
                    }
                }
                executePendingBindings()
            }
        }
    }
}

private class ActionDiffCallback : DiffUtil.ItemCallback<Action>() {

    override fun areItemsTheSame(
        oldItem: Action, newItem: Action
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Action, newItem: Action
    ): Boolean {
        return oldItem == newItem
    }
}