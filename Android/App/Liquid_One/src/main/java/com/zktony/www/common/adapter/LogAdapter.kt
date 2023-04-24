package com.zktony.www.common.adapter

import android.annotation.SuppressLint
import android.view.*
import androidx.recyclerview.widget.*
import com.zktony.core.R
import com.zktony.core.ext.*
import com.zktony.www.databinding.ItemLogBinding
import com.zktony.www.room.entity.Log

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 14:48
 */
class LogAdapter : ListAdapter<Log, LogViewHolder>(diffCallback) {

    var onDeleteButtonClick: (Log) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        return LogViewHolder(
            ItemLogBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onDeleteButtonClick
        )
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<Log>() {
            override fun areItemsTheSame(oldItem: Log, newItem: Log): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Log, newItem: Log): Boolean {
                return oldItem == newItem
            }
        }
    }
}

class LogViewHolder(
    private val binding: ItemLogBinding,
    private val onDeleteButtonClick: (Log) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    @SuppressLint("SetTextI18n")
    fun bind(item: Log) {
        binding.apply {
            log = item
            order.text = (layoutPosition + 1).toString()
            name.text = item.name
            status.text = when (item.status) {
                0 -> "/"
                1 -> "OK"
                else -> "/"
            }
            status.setTextColor(
                when (item.status) {
                    0 -> R.color.red
                    1 -> R.color.green
                    else -> R.color.red
                }
            )
            text.text = item.content
            with(delete) {
                clickScale()
                clickNoRepeat {
                    onDeleteButtonClick(item)
                }
            }
            with(spacer) {
                clickScale()
                clickNoRepeat {
                    if (content.visibility == View.GONE) {
                        content.visibility = View.VISIBLE
                        ivSpacer.setImageResource(R.mipmap.collapse_arrow)
                    } else {
                        content.visibility = View.GONE
                        ivSpacer.setImageResource(R.mipmap.expand_arrow)
                    }
                }
            }
            executePendingBindings()
        }
    }
}