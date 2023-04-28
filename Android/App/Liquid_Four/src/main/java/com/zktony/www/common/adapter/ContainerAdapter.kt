package com.zktony.www.common.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.zktony.core.ext.*
import com.zktony.www.databinding.ItemContainerBinding
import com.zktony.www.room.entity.Container

/**
 * @author: 刘贺贺
 * @date: 2022-09-21 11:27
 */
class ContainerAdapter : ListAdapter<Container, ContainerViewHolder>(diffCallback) {

    var onDeleteButtonClick: (Container) -> Unit = {}
    var onEditButtonClick: (Container) -> Unit = {}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContainerViewHolder {
        return ContainerViewHolder(
            ItemContainerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onDeleteButtonClick,
            onEditButtonClick,
        )
    }

    override fun onBindViewHolder(holder: ContainerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<Container>() {
            override fun areItemsTheSame(oldItem: Container, newItem: Container): Boolean {
                return oldItem.id == newItem.id
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Container, newItem: Container): Boolean {
                return oldItem == newItem
            }
        }
    }
}

class ContainerViewHolder(
    private val binding: ItemContainerBinding,
    private val onDeleteButtonClick: (Container) -> Unit,
    private val onEditButtonClick: (Container) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {
    @SuppressLint("SetTextI18n")
    fun bind(item: Container) {
        binding.apply {
            container = item
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