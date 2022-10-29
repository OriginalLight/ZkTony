package com.zktony.www.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.www.R
import com.zktony.www.common.extension.clickScale
import com.zktony.www.common.room.entity.Action
import com.zktony.www.common.room.entity.getActionEnum
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

    fun setOnDeleteButtonClick(onDeleteButtonClick: (Action) -> Unit) {
        this.onDeleteButtonClick = onDeleteButtonClick
    }

    class ViewHolder(
        private val binding: ItemActionBinding,
        private val onDeleteButtonClick: (Action) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Action) {
            binding.apply {
                action = item
                when (item.mode) {
                    0 -> {
                        icon.setBackgroundResource(R.mipmap.ic_blocking_liquid)
                        con4.visibility = View.GONE
                    }
                    1 -> {
                        icon.setBackgroundResource(R.mipmap.ic_antibody)
                        con4.visibility = View.GONE
                    }
                    2 -> {
                        icon.setBackgroundResource(R.mipmap.ic_antibody)
                        con4.visibility = View.GONE
                    }
                    3 -> {
                        icon.setBackgroundResource(R.mipmap.ic_washing)
                        con4.visibility = View.VISIBLE
                    }
                }
                model.text = getActionEnum(item.mode).value
                cardView.setOnClickListener {
                    PopTip.show(R.mipmap.item_delete, "点击右侧图标删除")
                }
                delete.run {
                    this.clickScale()
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
        oldItem: Action,
        newItem: Action
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Action,
        newItem: Action
    ): Boolean {
        return oldItem == newItem
    }
}