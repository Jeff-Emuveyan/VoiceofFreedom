package com.bellogate.voiceoffreedom.ui.media.video

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bellogate.voiceoffreedom.R
import com.bellogate.voiceoffreedom.model.User
import com.bellogate.voiceoffreedom.ui.SharedViewModel
import com.bellogate.voiceoffreedom.util.Fragments
import org.jetbrains.anko.support.v4.toast

class VideoFragment : Fragment() {

    private lateinit var viewModel: VideoViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private var user: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.video_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(VideoViewModel::class.java)
        sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel::class.java)

        viewModel.getUser(requireContext(), 1).observe(viewLifecycleOwner, Observer {
            user = it
            if(user != null && user!!.isAdmin){
                //this will cause the MainActivity to call 'onCreateOptionsMenu' again.
                //If the user is an Admin, the MainActivity will add a menu item to 'Add video'
                sharedViewModel.topMenuController.value = Fragments.VIDEO
            }
        })

        sharedViewModel.showAddVideoFragment.observe(viewLifecycleOwner, Observer {
            if(it){
                findNavController().navigate(R.id.action_nav_media_to_addVideoFragment)
            }
        })


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }


    override fun onPause() {
        super.onPause()
        //reset thr top menu by removing the "Add video" item:
        sharedViewModel.topMenuController.value = null
    }

}
