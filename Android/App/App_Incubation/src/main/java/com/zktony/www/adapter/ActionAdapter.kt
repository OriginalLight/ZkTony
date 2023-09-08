package com.zktony.www.adapter

import android.annotation.SuppressLint
import android.view.*
import androidx.recyclerview.widget.*
import com.zktony.core.R
import com.zktony.core.ext.*
import com.zktony.www.databinding.ItemActionBinding
import com.zktony.www.data.entities.Action
import com.zktony.www.data.entities.getActionEnum

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 11:27
 */
class ActionAdapter : ListAdapter<Action, ActionViewHolder>(diffCallback) {

    var onDeleteButtonClick: (Action) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionViewHolder {
        return ActionViewHolder(
            ItemActionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), onDeleteButtonClick
        )
    }

    override fun onBindViewHolder(holder: ActionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<Action>() {
            override fun areItemsTheSame(oldItem: Action, newItem: Action): Boolean {
                return oldItem.id == newItem.id
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Action, newItem: Action): Boolean {
                return oldItem == newItem
            }
        }
    }
}

class ActionViewHolder(
    private val binding: ItemActionBinding, private val onDeleteButtonClick: (Action) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    @SuppressLint("SetTextI18n")
    fun bind(item: Action) {
        binding.apply {
            action = item
            when (item.mode) {
                0 -> {
                    icon.setBackgroundResource(R.mipmap.box)
                    con4.visibility = View.GONE
                }

                3 -> {
                    icon.setBackgroundResource(R.mipmap.clean)
                    con4.visibility = View.VISIBLE
                }

                else -> {
                    icon.setBackgroundResource(R.mipmap.virus)
                    con4.visibility = View.GONE
                }
            }
            model.text = getActionEnum(item.mode).value
            time.text = item.time.format() + if (item.mode == 3) " Min" else " Hour"
            temperature.text = item.temp.format() + " ℃"
            water.text = item.volume.format() + " μL"
            counter.text = item.count.toString() + " Freq"
            cardView.clickNoRepeat {
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