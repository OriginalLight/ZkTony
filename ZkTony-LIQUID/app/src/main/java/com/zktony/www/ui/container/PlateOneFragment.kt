package com.zktony.www.ui.container

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.zktony.www.R
import com.zktony.www.base.BaseFragment
import com.zktony.www.common.extension.afterTextChange
import com.zktony.www.data.model.Pore
import com.zktony.www.databinding.FragmentPlateOneBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlateOneFragment :
    BaseFragment<PlateOneViewModel, FragmentPlateOneBinding>(R.layout.fragment_plate_one) {
    override val viewModel: PlateOneViewModel by viewModels()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initEditText()
        initPlate()
    }

    @SuppressLint("SetTextI18n")
    private fun initEditText() {
        binding.run {
            row.setText("8")
            column.setText("12")
            row.afterTextChange {
                val row = it.toIntOrNull() ?: 2
                dynamicPlate.setRow(maxOf(row, 2))
            }
            column.afterTextChange {
                val column = it.toIntOrNull() ?: 2
                dynamicPlate.setColumn(maxOf(column, 2))
            }
        }
    }

    private fun initPlate() {
        lifecycleScope.launch {
            delay(10 * 1000L)
            val data = mutableListOf<Pore>()
            for (i in 0 until binding.dynamicPlate.getColumn()) {
                if (i % 2 != 0) {
                    for (j in 0 until binding.dynamicPlate.getRow()) {
                        delay(2 * 1000L)
                        data.add(Pore(row = j, column = i, checked = true))
                        binding.dynamicPlate.setData(data)
                    }
                } else {
                    for (j in binding.dynamicPlate.getRow() - 1 downTo 0) {
                        delay(2 * 1000L)
                        data.add(Pore(row = j, column = i, checked = true))
                        binding.dynamicPlate.setData(data)
                    }
                }
            }
            binding.dynamicPlate.setData(listOf())
        }
    }
}