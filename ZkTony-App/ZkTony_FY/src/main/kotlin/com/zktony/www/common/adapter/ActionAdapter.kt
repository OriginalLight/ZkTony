package com.zktony.www.common.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kongzue.dialogx.dialogs.PopTip
import com.zktony.common.R
import com.zktony.common.ext.clickNoRepeat
import com.zktony.common.ext.clickScale
import com.zktony.common.ext.removeZero
import com.zktony.www.data.local.entity.Action
import com.zktony.www.data.local.entity.getActionEnum
import com.zktony.www.databinding.ItemActionBinding

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
            time.text = item.time.removeZero() + if (item.mode == 3) " 分钟" else " 小时"
            temperature.text = item.temperature.removeZero() + " ℃"
            water.text = item.liquidVolume.removeZero() + " 微升"
            counter.text = item.count.toString() + " 次"
            cardView.clickNoRepeat {
                PopTip.show("点击右侧图标删除")
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