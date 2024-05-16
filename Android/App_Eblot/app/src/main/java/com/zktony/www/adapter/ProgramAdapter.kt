package com.zktony.www.adapter

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.*
import com.zktony.core.ext.*
import com.zktony.www.R
import com.zktony.www.databinding.ItemProgramBinding
import com.zktony.www.data.entities.Program

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 11:27
 */
class ProgramAdapter : ListAdapter<Program, ProgramViewHolder>(diffCallback) {

    var selected: Program? = null
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            callback(value)
            notifyDataSetChanged()
        }

    var callback : (Program?) -> Unit = {}
    var onDoubleClick : (Program) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramViewHolder {
        return ProgramViewHolder(
            ItemProgramBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onItemClick = {
                selected = it
            },
            onItemDoubleClick = {
                selected = it
                onDoubleClick(it)
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ProgramViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.select(selected?.id == getItem(position).id)
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<Program>() {
            override fun areItemsTheSame(oldItem: Program, newItem: Program): Boolean {
                return oldItem.id == newItem.id
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Program, newItem: Program): Boolean {
                return oldItem == newItem
            }
        }
    }
}

@SuppressLint("SetTextI18n")
class ProgramViewHolder(
    private val binding: ItemProgramBinding,
    private val onItemClick: (Program) -> Unit,
    private val onItemDoubleClick: (Program) -> Unit = {},
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: Program) {
        binding.apply {
            program = item
            order.text = (layoutPosition + 1).toString()
            model.text =
                if (item.model == 0) itemView.context.getString(R.string.transfer) else itemView.context.getString(
                    R.string.dye
                )
            val str = StringBuilder()
            if (item.model == 0) {
                str.append(item.motor)
                str.append("RPM - ")
            }
            str.append(item.voltage.format())
            str.append("V - ")
            str.append(item.time.format())
            str.append("MIN")
            parameter.text = str.toString()

            itemView.setOnClickListener {

            }
            itemView.onDoubleClickListener(
                onClick = {
                    onItemClick(item)
                },
                onDoubleClick = {
                    onItemDoubleClick(item)
                }
            )
            executePendingBindings()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun select(bool: Boolean) {
        binding.apply {
            if (bool) {
                order.setBackgroundColor(itemView.context.getColor(R.color.orange))
                order.setTextColor(itemView.context.getColor(R.color.white))
                name.setBackgroundColor(itemView.context.getColor(R.color.orange))
                name.setTextColor(itemView.context.getColor(R.color.white))
                model.setBackgroundColor(itemView.context.getColor(R.color.orange))
                model.setTextColor(itemView.context.getColor(R.color.white))
                parameter.setBackgroundColor(itemView.context.getColor(R.color.orange))
                parameter.setTextColor(itemView.context.getColor(R.color.white))
            } else {
                order.setBackgroundColor(itemView.context.getColor(R.color.white))
                order.setTextColor(itemView.context.getColor(R.color.black))
                name.setBackgroundColor(itemView.context.getColor(R.color.white))
                name.setTextColor(itemView.context.getColor(R.color.black))
                model.setBackgroundColor(itemView.context.getColor(R.color.white))
                model.setTextColor(itemView.context.getColor(R.color.black))
                parameter.setBackgroundColor(itemView.context.getColor(R.color.white))
                parameter.setTextColor(itemView.context.getColor(R.color.black))
            }
        }
    }
}