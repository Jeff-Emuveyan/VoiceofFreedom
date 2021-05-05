package com.bellogate.voiceoffreedom.ui.media.audio

import android.net.Uri
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bellogate.voiceoffreedom.R
import com.bellogate.voiceoffreedom.model.Audio
import com.bellogate.voiceoffreedom.model.ListUIState
import com.bellogate.voiceoffreedom.model.User
import com.bellogate.voiceoffreedom.ui.SharedViewModel
import com.bellogate.voiceoffreedom.ui.media.video.setUpUIState
import com.bellogate.voiceoffreedom.util.*
import kotlinx.android.synthetic.main.audio_fragment.*
import kotlinx.android.synthetic.main.video_fragment.*
import kotlinx.android.synthetic.main.video_fragment.recyclerView
import java.io.File

class AudioFragment : Fragment() {

    companion object {
        fun newInstance() = AudioFragment()
    }

    private var audio: Audio? = null
    private lateinit var viewModel: AudioViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private var audioListAdapter: AudioListAdapter? = null
    private var user: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.audio_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AudioViewModel::class.java)
        sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel::class.java)

        viewModel.getUser(requireContext(), 1).observe(viewLifecycleOwner, Observer {
            user = it
            if(user != null && user!!.isAdmin){
                //this will cause the MainActivity to call 'onCreateOptionsMenu' again.
                //If the user is an Admin, the MainActivity will add a menu item to 'Add audio'
                sharedViewModel.topMenuController.value = Fragments.AUDIO
            }

            //fetch audio:
            fetchAudios(viewLifecycleOwner)
        })


        sharedViewModel.launchAudioPicker.observe(viewLifecycleOwner, Observer {
            if(it){
                //Open audio directory to allow user select an audio file:
                selectAudio(requireActivity()){uri, file ->

                    showAlert(requireContext(), "Upload?", "Do you want to upload:\n ${file.name}?"){
                        uploadAudio(file, uri)
                    }
                }
            }
        })

    }

    private fun fetchAudios(lifecycleOwner: LifecycleOwner) {
        //We use FirestorePagingAdapter to fetch the videos and also handle pagination
        //Do check the onStart() and onStop() to see how we handled lifecycle of the adapter
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        audioListAdapter = AudioListAdapter(requireContext(), viewModel.options(lifecycleOwner), user,
            uiState =  {
                when(it){
                    ListUIState.FOUND ->{
                        setUpUIState(ListUIState.FOUND)
                    }
                    ListUIState.NO_VIDEOS ->{
                        setUpUIState(ListUIState.NO_VIDEOS)
                    }
                    ListUIState.ERROR ->{
                        setUpUIState(ListUIState.ERROR)
                    }
                }

        }, audioItemClicked = {
                viewModel.playAudio(parentFragmentManager, it)
        }){audio ->
            this.audio = audio
            downloadFile(requireActivity(), this.audio!!.title!!, this.audio!!.audioUrl!!)
        }

        recyclerView.adapter = audioListAdapter
    }



    private fun setUpUIState(state: ListUIState) {

        progress.visibility = View.INVISIBLE

        when(state){

            ListUIState.FOUND ->{
                tvMessage.visibility = View.INVISIBLE
            }
            ListUIState.NO_VIDEOS ->{
                tvMessage.visibility = View.VISIBLE
                tvMessage.text = getString(R.string.no_audio_files)
            }
            ListUIState.ERROR ->{
                tvMessage.visibility = View.VISIBLE
                tvMessage.text = getString(R.string.network_error)
            }
        }
    }


    private fun uploadAudio(file: File, uri: Uri) {
        Toast.makeText(requireContext(), "Uploading...", Toast.LENGTH_LONG).show()
        if(file != null && uri != null){
            viewModel.uploadAudio(requireContext(), uri)
        }

    }


    override fun onResume() {
        super.onResume()

        if(user != null && user!!.isAdmin) {
            //since we are going to be launching an Activity to pick an audio file, we need to:
            //retain the "Add audio" item when the user (an Admin) returns back to this fragment:
            sharedViewModel.topMenuController.value = Fragments.AUDIO
            //We would't need to do this if it was a fragment we were launching. onActivityCreated will
            //be called when we return back to this fragment and the value of 'topMenuController' will
            //be set for us.

            //When we return back from the audio picker activity, we have to disable the live data that
            //launches the audio gallery:
            sharedViewModel.launchAudioPicker.value = false
        }
    }

    override fun onPause() {
        super.onPause()
        //reset the top menu by removing the "Change Image" item:
        sharedViewModel.topMenuController.value = null
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQUEST_DOWNLOAD_PERMISSIONS){
            if (grantResults.isNotEmpty()) {
                for (value in grantResults) {
                    if (value == -1) {
                        showAlert( "Permissions", "Please grant all permissions")
                        return
                    }
                }
                downloadFile(requireActivity(), audio!!.title!!, audio!!.audioUrl!!)
            }
        }
    }

}
