package com.zktony.www.ui.container

import android.os.Bundle
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import com.zktony.core.base.BaseFragment
import com.zktony.core.dialog.inputDialog
import com.zktony.core.dialog.messageDialog
import com.zktony.core.ext.clickNoRepeat
import com.zktony.core.ext.clickScale
import com.zktony.www.R
import com.zktony.www.common.adapter.ContainerAdapter
import com.zktony.www.databinding.FragmentContainerBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContainerFragment :
    BaseFragment<ContainerViewModel, FragmentContainerBinding>(R.layout.fragment_container) {

    override val viewModel: ContainerViewModel by viewModel()

    private val adapter by lazy { ContainerAdapter() }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        initFlowCollector()
        initView()
    }

    private fun initFlowCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    adapter.submitList(it.list)
                }
            }
        }
    }

    /**
     * 初始化view
     */
    private fun initView() {
        adapter.onDeleteButtonClick = {
            messageDialog(
                title = getString(com.zktony.core.R.string.delete),
                message = "${getString(com.zktony.core.R.string.whether_delete)} ${it.name} ?",
                block = { viewModel.delete(it) }
            )
        }
        adapter.onEditButtonClick = {
            val directions =
                if (it.type == 0) R.id.action_navigation_container_to_navigation_wash else R.id.action_navigation_container_to_navigation_container_edit
            findNavController().navigate(
                directions,
                Bundle().apply { putLong("id", it.id) }
            )
        }
        binding.apply {
            recyclerView.adapter = adapter
            with(add) {
                clickScale()
                clickNoRepeat {
                    inputDialog(
                        title = getString(com.zktony.core.R.string.add),
                        hint = getString(com.zktony.core.R.string.input_name),
                        block = {
                            viewModel.insert(it) {
                                findNavController().navigate(
                                    R.id.action_navigation_container_to_navigation_container_edit,
                                    Bundle().apply { putLong("id", it) }
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}