package com.zktony.www.common.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.zktony.core.ext.*
import com.zktony.www.databinding.ItemPointBinding
import com.zktony.www.room.entity.Point

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 11:27
 */
class PointAdapter : ListAdapter<Point, PointViewHolder>(diffCallback) {

    var onButtonClick: (Point) -> Unit = {}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointViewHolder {
        return PointViewHolder(
            ItemPointBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onButtonClick
        )
    }

    override fun onBindViewHolder(holder: PointViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<Point>() {
            override fun areItemsTheSame(oldItem: Point, newItem: Point): Boolean {
                return oldItem.id == newItem.id
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Point, newItem: Point): Boolean {
                return oldItem == newItem
            }
        }
    }
}

class PointViewHolder(
    private val binding: ItemPointBinding,
    private val onButtonClick: (Point) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {
    @SuppressLint("SetTextI18n")
    fun bind(item: Point) {
        binding.apply {
            content.text = "${'A' + item.x}${item.y + 1}: ( ${
                String.format("%.2f", item.xAxis).format()
            }, ${String.format("%.2f", item.yAxis).format()} )"
            with(move) {
                clickScale()
                clickNoRepeat {
                    onButtonClick.invoke(item)
                }
            }
            executePendingBindings()
        }
    }
}