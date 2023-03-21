package com.zktony.www.common.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zktony.common.R
import com.zktony.common.ext.clickNoRepeat
import com.zktony.www.data.local.room.entity.Motor
import com.zktony.www.databinding.ItemMotorBinding

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 11:27
 */
class MotorAdapter : ListAdapter<Motor, MotorAdapter.ViewHolder>(MotorDiffCallback()) {

    private var onEditButtonClick: (Motor) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemMotorBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onEditButtonClick
        )
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setOnEditButtonClick(onEditButtonClick: (Motor) -> Unit) {
        this.onEditButtonClick = onEditButtonClick
    }


    @SuppressLint("SetTextI18n")
    class ViewHolder(
        private val binding: ItemMotorBinding,
        private val onEditButtonClick: (Motor) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Motor) {
            binding.apply {
                motor = item
                icon.setImageResource(if (item.id < 2) R.mipmap.motor else R.mipmap.pump)
                cardView.clickNoRepeat { onEditButtonClick(item) }
                executePendingBindings()
            }
        }
    }
}

private class MotorDiffCallback : DiffUtil.ItemCallback<Motor>() {

    override fun areItemsTheSame(
        oldItem: Motor,
        newItem: Motor
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Motor,
        newItem: Motor
    ): Boolean {
        return oldItem == newItem
    }
}