package com.zktony.www.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zktony.www.common.extension.clickScale
import com.zktony.www.common.extension.removeZero
import com.zktony.www.common.room.entity.Program
import com.zktony.www.databinding.ItemProgramBinding

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 11:27
 */
class ProgramAdapter : ListAdapter<Program, ProgramAdapter.ViewHolder>(ProgramDiffCallback()) {

    private lateinit var onDeleteButtonClick: (Program) -> Unit
    private lateinit var onEditButtonClick: (Program) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemProgramBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onDeleteButtonClick,
            onEditButtonClick
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setOnDeleteButtonClick(onDeleteButtonClick: (Program) -> Unit) {
        this.onDeleteButtonClick = onDeleteButtonClick
    }

    fun setOnEditButtonClick(onEditButtonClick: (Program) -> Unit) {
        this.onEditButtonClick = onEditButtonClick
    }


    @SuppressLint("SetTextI18n")
    class ViewHolder(
        private val binding: ItemProgramBinding,
        private val onDeleteButtonClick: (Program) -> Unit,
        private val onEditButtonClick: (Program) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Program) {
            binding.apply {
                program = item
                order.text = (layoutPosition + 1).toString()
                val str = StringBuilder()
                if (item.model == 0) {
                    str.append(item.motor)
                    str.append("RPM - ")
                }
                str.append(item.voltage.toString().removeZero())
                str.append("V - ")
                str.append(item.time.toString().removeZero())
                str.append("MIN")
                parameter.text = str.toString()
                delete.run {
                    clickScale()
                    setOnClickListener { onDeleteButtonClick(item) }
                }
                edit.run {
                    clickScale()
                    setOnClickListener { onEditButtonClick(item) }
                }
                if (layoutPosition % 2 == 0) {
                    order.setBackgroundColor(Color.parseColor("#F5F5F5"))
                    name.setBackgroundColor(Color.parseColor("#F5F5F5"))
                    model.setBackgroundColor(Color.parseColor("#F5F5F5"))
                    parameter.setBackgroundColor(Color.parseColor("#F5F5F5"))
                }
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