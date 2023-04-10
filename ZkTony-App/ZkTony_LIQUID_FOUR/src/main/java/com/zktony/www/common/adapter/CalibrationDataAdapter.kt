package com.zktony.www.common.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zktony.core.ext.clickNoRepeat
import com.zktony.core.ext.clickScale
import com.zktony.core.ext.removeZero
import com.zktony.www.room.entity.CalibrationData
import com.zktony.www.databinding.ItemCalibrationDataBinding

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 11:27
 */
class CalibrationDataAdapter :
    ListAdapter<CalibrationData, CalibrationDataViewHolder>(diffCallback) {

    var onDeleteButtonClick: (CalibrationData) -> Unit = {}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalibrationDataViewHolder {
        return CalibrationDataViewHolder(
            ItemCalibrationDataBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onDeleteButtonClick,
        )
    }

    override fun onBindViewHolder(holder: CalibrationDataViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<CalibrationData>() {
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
    }

}

class CalibrationDataViewHolder(
    private val binding: ItemCalibrationDataBinding,
    private val onDeleteButtonClick: (CalibrationData) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {
    @SuppressLint("SetTextI18n")
    fun bind(item: CalibrationData) {
        binding.apply {
            cali = item
            order.text = (layoutPosition + 1).toString()
            name.text = when (item.pumpId) {
                0 -> "泵一"
                1 -> "泵二"
                2 -> "泵三"
                3 -> "泵四"
                else -> "未知"
            }
            expect.text = item.expect.toString().removeZero()
            actual.text = item.actual.toString().removeZero()
            with(delete) {
                clickScale()
                clickNoRepeat {
                    onDeleteButtonClick.invoke(item)
                }
            }
            executePendingBindings()
        }
    }
}