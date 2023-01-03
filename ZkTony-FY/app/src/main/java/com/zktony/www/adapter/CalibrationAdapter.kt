package com.zktony.www.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zktony.www.common.extension.clickScale
import com.zktony.www.data.model.Calibration
import com.zktony.www.databinding.ItemCalibrationBinding

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 11:27
 */
class CalibrationAdapter :
    ListAdapter<Calibration, CalibrationAdapter.ViewHolder>(CalibrationDiffCallback()) {

    private lateinit var onDeleteButtonClick: (Calibration) -> Unit
    private lateinit var onEditButtonClick: (Calibration) -> Unit
    private lateinit var onSelectClick: (Calibration) -> Unit


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCalibrationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onDeleteButtonClick,
            onEditButtonClick,
            onSelectClick
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setOnDeleteButtonClick(onDeleteButtonClick: (Calibration) -> Unit) {
        this.onDeleteButtonClick = onDeleteButtonClick
    }

    fun setOnEditButtonClick(onEditButtonClick: (Calibration) -> Unit) {
        this.onEditButtonClick = onEditButtonClick
    }

    fun setOnSelectClick(onSelectClick: (Calibration) -> Unit) {
        this.onSelectClick = onSelectClick
    }


    class ViewHolder(
        private val binding: ItemCalibrationBinding,
        private val onDeleteButtonClick: (Calibration) -> Unit,
        private val onEditButtonClick: (Calibration) -> Unit,
        private val onSelectClick: (Calibration) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: Calibration) {
            binding.apply {
                cali = item
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
                select.run {
                    this.clickScale()
                    this.setOnClickListener {
                        if (item.status == 0) {
                            onSelectClick.invoke(item)
                        }
                    }
                }
                if (layoutPosition % 2 == 0) {
                    order.setBackgroundColor(Color.parseColor("#F5F5F5"))
                    name.setBackgroundColor(Color.parseColor("#F5F5F5"))
                }
                executePendingBindings()
            }
        }
    }
}

private class CalibrationDiffCallback : DiffUtil.ItemCallback<Calibration>() {

    override fun areItemsTheSame(
        oldItem: Calibration,
        newItem: Calibration
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Calibration,
        newItem: Calibration
    ): Boolean {
        return oldItem == newItem
    }
}