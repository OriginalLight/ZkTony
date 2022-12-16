package com.zktony.www.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zktony.www.common.extension.clickScale
import com.zktony.www.common.extension.removeZero
import com.zktony.www.common.room.entity.CalibrationData
import com.zktony.www.databinding.ItemCalibrationDataBinding

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 11:27
 */
class CalibrationDataAdapter :
    ListAdapter<CalibrationData, CalibrationDataAdapter.ViewHolder>(CalibrationDataDiffCallback()) {

    private lateinit var onDeleteButtonClick: (CalibrationData) -> Unit


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCalibrationDataBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onDeleteButtonClick,
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setOnDeleteButtonClick(onDeleteButtonClick: (CalibrationData) -> Unit) {
        this.onDeleteButtonClick = onDeleteButtonClick
    }


    class ViewHolder(
        private val binding: ItemCalibrationDataBinding,
        private val onDeleteButtonClick: (CalibrationData) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: CalibrationData) {
            binding.apply {
                cali = item
                order.text = (layoutPosition + 1).toString()
                name.text = when (item.motorId) {
                    3 -> "泵一"
                    4 -> "泵二"
                    5 -> "泵三"
                    6 -> "泵四"
                    7 -> "泵五"
                    else -> "未知"
                }
                volume.text = item.volume.toString().removeZero()
                actualVolume.text = item.actualVolume.toString().removeZero()
                delete.run {
                    this.clickScale()
                    this.setOnClickListener {
                        onDeleteButtonClick.invoke(item)
                    }
                }
                executePendingBindings()
            }
        }
    }
}

private class CalibrationDataDiffCallback : DiffUtil.ItemCallback<CalibrationData>() {

    override fun areItemsTheSame(
        oldItem: CalibrationData,
        newItem: CalibrationData
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: CalibrationData,
        newItem: CalibrationData
    ): Boolean {
        return oldItem == newItem
    }
}