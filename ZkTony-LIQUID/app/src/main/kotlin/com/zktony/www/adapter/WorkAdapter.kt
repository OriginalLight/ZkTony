package com.zktony.www.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zktony.www.common.extension.clickScale
import com.zktony.www.common.room.entity.Work
import com.zktony.www.databinding.ItemWorkBinding

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 11:27
 */
class WorkAdapter :
    ListAdapter<Work, WorkAdapter.ViewHolder>(WorkDiffCallback()) {

    private var onDeleteButtonClick: (Work) -> Unit = {}
    private var onEditButtonClick: (Work) -> Unit = {}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemWorkBinding.inflate(
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

    fun setOnDeleteButtonClick(onDeleteButtonClick: (Work) -> Unit) {
        this.onDeleteButtonClick = onDeleteButtonClick
    }

    fun setOnEditButtonClick(onEditButtonClick: (Work) -> Unit) {
        this.onEditButtonClick = onEditButtonClick
    }

    class ViewHolder(
        private val binding: ItemWorkBinding,
        private val onDeleteButtonClick: (Work) -> Unit,
        private val onEditButtonClick: (Work) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: Work) {
            binding.apply {
                work = item
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

private class WorkDiffCallback : DiffUtil.ItemCallback<Work>() {
    override fun areItemsTheSame(
        oldItem: Work,
        newItem: Work
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Work,
        newItem: Work
    ): Boolean {
        return oldItem == newItem
    }
}