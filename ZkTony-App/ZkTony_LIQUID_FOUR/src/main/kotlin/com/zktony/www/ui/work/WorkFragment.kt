package com.zktony.www.ui.work

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.zktony.common.base.BaseFragment
import com.zktony.common.dialog.deleteDialog
import com.zktony.common.dialog.inputDialog
import com.zktony.common.ext.clickScale
import com.zktony.www.R
import com.zktony.www.adapter.WorkAdapter
import com.zktony.www.databinding.FragmentWorkBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WorkFragment : BaseFragment<WorkViewModel, FragmentWorkBinding>(R.layout.fragment_work) {
    override val viewModel: WorkViewModel by viewModels()

    private val adapter by lazy { WorkAdapter() }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()
    }

    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.workList.collect {
                    adapter.submitList(it)
                }
            }
        }
    }

    private fun initView() {
        adapter.apply {
            setOnDeleteButtonClick {
                deleteDialog(name = it.name, block = { viewModel.delete(it) })
            }
            setOnEditButtonClick {
                findNavController().navigate(
                    directions = WorkFragmentDirections.actionNavigationWorkToNavigationWorkPlate(it.id)
                )
            }
        }
        binding.apply {
            binding.apply {
                recyclerView.adapter = adapter
                with(add) {
                    clickScale()
                    setOnClickListener {
                        inputDialog { viewModel.insert(it) }
                    }
                }
            }
        }
    }
}