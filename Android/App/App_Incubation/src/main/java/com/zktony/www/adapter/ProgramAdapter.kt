package com.zktony.www.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.zktony.core.ext.clickNoRepeat
import com.zktony.core.ext.clickScale
import com.zktony.www.databinding.ItemProgramBinding
import com.zktony.www.data.entities.Program

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 11:27
 */
class ProgramAdapter : ListAdapter<Program, ProgramViewHolder>(diffCallback) {

    var onDeleteButtonClick: (Program) -> Unit = {}
    var onEditButtonClick: (Program) -> Unit = {}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramViewHolder {
        return ProgramViewHolder(
            ItemProgramBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), onDeleteButtonClick, onEditButtonClick
        )
    }

    override fun onBindViewHolder(holder: ProgramViewHolder, position: Int) {
        holder.bind(getItem(position))
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

class ProgramViewHolder(
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