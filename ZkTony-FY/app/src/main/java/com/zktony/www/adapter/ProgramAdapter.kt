package com.zktony.www.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zktony.www.common.extension.clickScale
import com.zktony.www.data.model.Program
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


    class ViewHolder(
        private val binding: ItemProgramBinding,
        private val onDeleteButtonClick: (Program) -> Unit,
        private val onEditButtonClick: (Program) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: Program) {
            binding.apply {
                program = item
                order.text = (layoutPosition + 1).toString()
                edit.run {
                    this.clickScale()
                    this.setOnClickListener {
                        onEditButtonClick.invoke(item)
                    }
                }
                delete.run {
                    this.clickScale()
                    this.setOnClickListener {
                        onDeleteButtonClick.invoke(item)
                    }
                }
                if (layoutPosition % 2 == 0) {
                    order.setBackgroundColor(Color.parseColor("#F5F5F5"))
                    name.setBackgroundColor(Color.parseColor("#F5F5F5"))
                    actions.setBackgroundColor(Color.parseColor("#F5F5F5"))
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

    override fun areContentsTheSame(
        oldItem: Program,
        newItem: Program
    ): Boolean {
        return oldItem == newItem
    }
}