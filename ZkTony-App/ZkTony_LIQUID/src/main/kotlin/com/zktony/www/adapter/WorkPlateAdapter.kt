package com.zktony.www.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zktony.common.extension.clickScale
import com.zktony.www.data.local.room.entity.WorkPlate
import com.zktony.www.databinding.ItemWorkPlateBinding

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 11:27
 */
class WorkPlateAdapter :
    ListAdapter<WorkPlate, WorkPlateAdapter.ViewHolder>(WorkPlateDiffCallback()) {

    private var onEditButtonClick: (WorkPlate) -> Unit = {}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemWorkPlateBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onEditButtonClick
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    fun setOnEditButtonClick(onEditButtonClick: (WorkPlate) -> Unit) {
        this.onEditButtonClick = onEditButtonClick
    }

    class ViewHolder(
        private val binding: ItemWorkPlateBinding,
        private val onEditButtonClick: (WorkPlate) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: WorkPlate) {
            binding.apply {
                name.text = when (item.sort) {
                    0 -> "一号板"
                    1 -> "二号板"
                    2 -> "三号板"
                    3 -> "四号板"
                    else -> "未知板"
                }
                size.text = "${item.row} X ${item.column}"
                select.text = "${item.count}"
                with(edit) {
                    clickScale()
                    setOnClickListener {
                        onEditButtonClick.invoke(item)
                    }
                }
                executePendingBindings()
            }
        }
    }
}

private class WorkPlateDiffCallback : DiffUtil.ItemCallback<WorkPlate>() {
    override fun areItemsTheSame(
        oldItem: WorkPlate,
        newItem: WorkPlate
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: WorkPlate,
        newItem: WorkPlate
    ): Boolean {
        return oldItem == newItem
    }
}