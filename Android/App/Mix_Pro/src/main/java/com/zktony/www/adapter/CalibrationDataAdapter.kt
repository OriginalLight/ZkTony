package com.zktony.www.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.zktony.core.ext.*
import com.zktony.www.R
import com.zktony.www.databinding.ItemCalibrationDataBinding
import com.zktony.www.data.entities.CalibrationData

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

            @SuppressLint("DiffUtilEquals")
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
            name.text = when (item.index) {
                0 -> "促凝剂泵"
                1 -> "一号重液泵"
                2 -> "一号轻液泵"
                3 -> "二号重液泵"
                4 -> "二号轻液泵"
                5 -> "三号重液泵"
                6 -> "三号轻液泵"
                else -> "注射泵"
            }
            actual.text = item.actual.format()
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