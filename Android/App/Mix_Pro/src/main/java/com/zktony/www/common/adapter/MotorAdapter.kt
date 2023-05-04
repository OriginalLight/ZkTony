package com.zktony.www.common.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.zktony.core.R
import com.zktony.core.ext.clickNoRepeat
import com.zktony.www.databinding.ItemMotorBinding
import com.zktony.www.room.entity.Motor

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 11:27
 */
class MotorAdapter : ListAdapter<Motor, MotorViewHolder>(diffCallback) {

    var onEditButtonClick: (Motor) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MotorViewHolder {
        return MotorViewHolder(
            ItemMotorBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onEditButtonClick
        )
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: MotorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<Motor>() {
            override fun areItemsTheSame(oldItem: Motor, newItem: Motor): Boolean {
                return oldItem.id == newItem.id
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Motor, newItem: Motor): Boolean {
                return oldItem == newItem
            }
        }

    }
}

class MotorViewHolder(
    private val binding: ItemMotorBinding,
    private val onEditButtonClick: (Motor) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: Motor) {
        binding.apply {
            motor = item
            cardView.clickNoRepeat { onEditButtonClick(item) }
            executePendingBindings()
        }
    }
}