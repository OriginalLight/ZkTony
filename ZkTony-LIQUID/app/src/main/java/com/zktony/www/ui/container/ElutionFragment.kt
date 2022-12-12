package com.zktony.www.ui.container

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zktony.www.R

class ElutionFragment : Fragment() {

    companion object {
        fun newInstance() = ElutionFragment()
    }

    private lateinit var viewModel: ElutionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_elution, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ElutionViewModel::class.java)
        // TODO: Use the ViewModel
    }

}