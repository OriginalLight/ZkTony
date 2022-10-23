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
import com.zktony.www.common.model.Event
import com.zktony.www.data.entity.Program
import com.zktony.www.databinding.ItemProgramBinding
import org.greenrobot.eventbus.EventBus

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 11:27
 */
class ProgramAdapter : ListAdapter<Program, ProgramAdapter.ViewHolder>(ProgramDiffCallback()) {
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
            EventBus.getDefault().post(Event(Constants.BLANK, Constants.PROGRAM_CLICK))
            notifyDataSetChanged()
        }
    }

    fun getItem(): Program {
        return getItem(currentPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = ViewHolder(
            ItemProgramBinding.inflate(
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
        private val binding: ItemProgramBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Program) {
            binding.apply {
                program = item
                tv1.text = (layoutPosition + 1).toString()
                val str = StringBuilder()
                if (item.model == 0) {
                    str.append(item.motor)
                    str.append("RPM-")
                }
                str.append(item.voltage.toString().removeZero())
                str.append("V-")
                str.append(item.time.toString().removeZero())
                str.append("MIN")
                tv4.text = str.toString()
                executePendingBindings()
            }
        }
    }
}

private class ProgramDiffCallback : DiffUtil.ItemCallback<Program>() {

    override fun areItemsTheSame(
        oldItem: Program,
        newItem: Program
    ): Boolean {
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(
        oldItem: Program,
        newItem: Program
    ): Boolean {
        return oldItem == newItem
    }
}