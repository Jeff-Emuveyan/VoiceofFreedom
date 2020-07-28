package com.bellogate.voiceoffreedom.ui.media.video

import android.annotation.SuppressLint
import android.view.View
import com.bellogate.voiceoffreedom.model.Video
import com.bellogate.voiceoffreedom.model.ListUIState
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.android.synthetic.main.video_fragment.*

fun VideoFragment.setUpUIState(uiState: ListUIState){

    when(uiState){
        ListUIState.LOADING ->{
            exoPlayerView.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
            tvError.visibility = View.INVISIBLE
        }

        ListUIState.FOUND->{
            exoPlayerView.visibility = View.VISIBLE
            progressBar.visibility = View.INVISIBLE
            tvError.visibility = View.INVISIBLE
        }

        ListUIState.NO_VIDEOS->{
            exoPlayerView.visibility = View.INVISIBLE
            progressBar.visibility = View.INVISIBLE
            tvError.visibility = View.VISIBLE
            tvError.text = "No videos to show"
            textViewTitle.text = ""
        }
        ListUIState.ERROR ->{
            exoPlayerView.visibility = View.INVISIBLE
            progressBar.visibility = View.INVISIBLE
            tvError.visibility = View.VISIBLE
            tvError.text = "Network error"
            textViewTitle.text = ""
        }
    }

}


@SuppressLint("InlinedApi")
fun VideoFragment.hideSystemUi() {
    exoPlayerView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
}


fun VideoFragment.initializePlayer(){
    player = SimpleExoPlayer.Builder(requireContext()).build()
    exoPlayerView.player = player

    player?.addListener(listener)
    player?.playWhenReady = true;
    player?.seekTo(currentWindow, playbackPosition);
}


fun VideoFragment.playVideo(video: Video){
    player?.prepare(viewModel.getMediaSource(requireContext(),
        video.videoUrl!!),
        false, false)

    textViewTitle.text = video.title
}


fun VideoFragment.releasePlayer() {
    if (player != null) {
        player?.removeListener(listener)
        playWhenReady = player!!.playWhenReady
        playbackPosition = player!!.currentPosition
        currentWindow = player!!.currentWindowIndex
        player!!.release()
        player = null
    }
}