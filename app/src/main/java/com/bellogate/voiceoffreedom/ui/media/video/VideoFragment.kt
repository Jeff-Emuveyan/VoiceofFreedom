package com.bellogate.voiceoffreedom.ui.media.video

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bellogate.voiceoffreedom.R
import com.bellogate.voiceoffreedom.model.User
import com.bellogate.voiceoffreedom.model.ListUIState
import com.bellogate.voiceoffreedom.ui.SharedViewModel
import com.bellogate.voiceoffreedom.util.Fragments
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.video_fragment.*


class VideoFragment : Fragment() {

    lateinit var viewModel: VideoViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private var videoListAdapter: VideoListAdapter? = null
    var player: SimpleExoPlayer? = null
    private var user: User? = null
    var playWhenReady = true
    var currentWindow = 0
    var playbackPosition: Long = 0
    lateinit var listener: PlayStateListener


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

            //fetch videos:
            fetchVideos(viewLifecycleOwner)
        })

        sharedViewModel.showAddVideoFragment.observe(viewLifecycleOwner, Observer {
            if(it){
                findNavController().navigate(R.id.action_nav_media_to_addVideoFragment)
            }
        })

        setUpUIState(ListUIState.LOADING)//default until a response comes.
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listener = PlayStateListener()
    }


    private fun fetchVideos(lifecycleOwner: LifecycleOwner) {
        //We use FirestorePagingAdapter to fetch the videos and also handle pagination
        //Do check the onStart() and onStop() to see how we handled lifecycle of the adapter
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        videoListAdapter = VideoListAdapter(requireContext(), viewModel.options(lifecycleOwner), user,
            uiState = {uiState : ListUIState ->
                when(uiState){
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
            }, firstVideoReady = {
                playVideo(it)
            }, videoItemClicked = {
                playVideo(it)
            } )

        recyclerView.adapter = videoListAdapter
    }


    override fun onPause() {
        super.onPause()
        //reset thr top menu by removing the "Add video" item:
        sharedViewModel.topMenuController.value = null

        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()

        //A line of code to handle fragment lifecycle of FirestorePagingAdapter
        videoListAdapter?.stopListening();

        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }


    override fun onStart() {
        super.onStart()

        //A line of code to handle fragment lifecycle of FirestorePagingAdapter
        videoListAdapter?.startListening()

        if (Util.SDK_INT >= 24) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if (Util.SDK_INT < 24 || player == null) {
            initializePlayer()
        }
    }


    inner class PlayStateListener : Player.EventListener{

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if(playbackState == ExoPlayer.STATE_BUFFERING){
                setUpUIState(ListUIState.LOADING)

            }else if(playbackState == ExoPlayer.STATE_READY){
                setUpUIState(ListUIState.FOUND)
            }

            if(playbackState == ExoPlayer.STATE_ENDED){
                player?.seekTo(0); //if the video has finished playing, restart it.
            }
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            setUpUIState(ListUIState.ERROR)
        }
    }
}
