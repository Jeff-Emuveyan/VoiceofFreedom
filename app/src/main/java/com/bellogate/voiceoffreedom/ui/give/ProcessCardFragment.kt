package com.bellogate.voiceoffreedom.ui.give

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bellogate.voiceoffreedom.R

class ProcessCardFragment : Fragment() {

    companion object {
        fun newInstance() = ProcessCardFragment()
    }

    private lateinit var viewModel: ProcessCardViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.process_card_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ProcessCardViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
