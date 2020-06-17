package com.bellogate.voiceoffreedom.ui.media.video.add

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bellogate.voiceoffreedom.R

class AddVideoFragment : Fragment() {

    companion object {
        fun newInstance() = AddVideoFragment()
    }

    private lateinit var viewModel: AddVideoViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_video_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AddVideoViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
