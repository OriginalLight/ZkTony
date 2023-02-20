package com.zktony.www.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zktony.common.extension.clickScale
import com.zktony.www.data.local.room.entity.Program
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
                LayoutInflater.from(parent.context), parent, false
            ), onDeleteButtonClick, onEditButtonClick
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
                with(edit) {
                    clickScale()
                    setOnClickListener {
                        onEditButtonClick.invoke(item)
                    }
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

private class ProgramDiffCallback : DiffUtil.ItemCallback<Program>() {

    override fun areItemsTheSame(
        oldItem: Program, newItem: Program
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Program, newItem: Program
    ): Boolean {
        return oldItem == newItem
    }
}