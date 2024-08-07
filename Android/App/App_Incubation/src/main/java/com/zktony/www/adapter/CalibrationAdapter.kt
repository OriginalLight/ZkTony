package com.zktony.www.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.*
import com.zktony.core.ext.clickNoRepeat
import com.zktony.core.ext.clickScale
import com.zktony.www.databinding.ItemCalibrationBinding
import com.zktony.www.data.entities.Calibration

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 11:27
 */
class CalibrationAdapter : ListAdapter<Calibration, CalibrationViewHolder>(diffCallback) {

    var onDeleteButtonClick: (Calibration) -> Unit = {}
    var onEditButtonClick: (Calibration) -> Unit = {}
    var onCheckedClick: (Calibration) -> Unit = {}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalibrationViewHolder {
        return CalibrationViewHolder(
            ItemCalibrationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onDeleteButtonClick,
            onEditButtonClick,
            onCheckedClick
        )
    }

    override fun onBindViewHolder(holder: CalibrationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<Calibration>() {
            override fun areItemsTheSame(oldItem: Calibration, newItem: Calibration): Boolean {
                return oldItem.id == newItem.id
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Calibration, newItem: Calibration): Boolean {
                return oldItem == newItem
            }
        }
    }
}

class CalibrationViewHolder(
    private val binding: ItemCalibrationBinding,
    private val onDeleteButtonClick: (Calibration) -> Unit,
    private val onEditButtonClick: (Calibration) -> Unit,
    private val onCheckedClick: (Calibration) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    @SuppressLint("SetTextI18n")
    fun bind(item: Calibration) {
        binding.apply {
            cali = item
            order.text = (layoutPosition + 1).toString()
            delete.isVisible = item.name != itemView.context.getString(com.zktony.core.R.string.def)
            with(select) {
                clickScale()
                clickNoRepeat {
                    onCheckedClick.invoke(item)
                }
            }
            with(edit) {
                clickScale()
                clickNoRepeat {
                    onEditButtonClick.invoke(item)
                }
            }
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