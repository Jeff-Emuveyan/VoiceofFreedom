package com.bellogate.voiceoffreedom.ui.devotional

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bellogate.voiceoffreedom.R

class DevotionalFragment : Fragment() {

    companion object {
        fun newInstance() = DevotionalFragment()
    }

    private lateinit var viewModel: DevotionalViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.devotional_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DevotionalViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
