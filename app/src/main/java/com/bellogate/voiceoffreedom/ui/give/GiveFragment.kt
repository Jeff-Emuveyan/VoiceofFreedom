package com.bellogate.voiceoffreedom.ui.give

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bellogate.voiceoffreedom.R

class GiveFragment : Fragment() {

    companion object {
        fun newInstance() = GiveFragment()
    }

    private lateinit var viewModel: GiveViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.give_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(GiveViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
