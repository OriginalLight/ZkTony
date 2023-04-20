package com.zktony.www.ui.program

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import com.zktony.core.base.BaseFragment
import com.zktony.core.dialog.inputDialog
import com.zktony.core.dialog.messageDialog
import com.zktony.core.ext.clickNoRepeat
import com.zktony.core.ext.clickScale
import com.zktony.www.R
import com.zktony.www.common.adapter.ProgramAdapter
import com.zktony.www.databinding.FragmentProgramBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProgramFragment :
    BaseFragment<ProgramViewModel, FragmentProgramBinding>(R.layout.fragment_program) {
    override val viewModel: ProgramViewModel by viewModel()

    private val adapter by lazy { ProgramAdapter() }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()
    }

    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.workList.collect {
                    adapter.submitList(it)
                    binding.apply {
                        recyclerView.isVisible = it.isNotEmpty()
                        empty.isVisible = it.isEmpty()
                    }
                }
            }
        }
    }

    private fun initView() {
        adapter.apply {
            onDeleteButtonClick = {
                messageDialog(
                    title = getString(com.zktony.core.R.string.delete),
                    message = "${getString(com.zktony.core.R.string.whether_delete)} ${it.name} ?",
                    block = { viewModel.delete(it) }
                )
            }
            onEditButtonClick = {
                findNavController().navigate(
                    R.id.action_navigation_program_to_navigation_program_edit,
                    Bundle().apply { putLong("id", it.id) }
                )
            }
        }
        binding.apply {
            binding.apply {
                recyclerView.adapter = adapter
                with(add) {
                    clickScale()
                    clickNoRepeat {
                        inputDialog(
                            title = getString(com.zktony.core.R.string.add),
                            hint = getString(com.zktony.core.R.string.input_name),
                            block = {
                                viewModel.insert(it) { id ->
                                    findNavController().navigate(
                                        R.id.action_navigation_program_to_navigation_program_edit,
                                        Bundle().apply { putLong("id", id) })
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}