package com.bellogate.voiceoffreedom.ui.media.audio

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.bellogate.voiceoffreedom.R
import com.bellogate.voiceoffreedom.model.Audio
import com.bellogate.voiceoffreedom.model.ListUIState
import com.bellogate.voiceoffreedom.ui.media.video.*
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.audio_player_layout.*
import kotlinx.android.synthetic.main.audio_player_layout.exoPlayerView
import kotlinx.android.synthetic.main.audio_player_layout.progressBar
import kotlinx.android.synthetic.main.video_fragment.*

class AudioPlayerDialogFragment(var audio: Audio): DialogFragment() {

    lateinit var viewModel: AudioPlayerDialogFragmentModel
    var player: SimpleExoPlayer? = null
    lateinit var listener: PlayStateListener
    var currentWindow = 0
    var playbackPosition: Long = 0
    var playWhenReady = true

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        if (dialog.window != null) {
            dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        }
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo_Light)
        return inflater.inflate(R.layout.audio_player_layout, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(AudioPlayerDialogFragmentModel::class.java)

    }


    override fun onStart() {
        super.onStart()
        //make dialog full screen:
        dialog?.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

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


    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(requireContext()).build()
        exoPlayerView.player = player
        listener = PlayStateListener()

        player?.addListener(listener)
        player?.playWhenReady = true;
        player?.seekTo(currentWindow, playbackPosition)

        //play the audio:
        player?.prepare(viewModel.getMediaSource(requireContext(),
            audio.audioUrl!!),
            false, false)
    }



    private fun releasePlayer() {
        if (player != null) {
            player?.removeListener(listener)
            playWhenReady = player!!.playWhenReady
            playbackPosition = player!!.currentPosition
            currentWindow = player!!.currentWindowIndex
            player!!.release()
            player = null
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    @SuppressLint("InlinedApi")
    fun hideSystemUi() {
        exoPlayerView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    inner class PlayStateListener : Player.EventListener{

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if(playbackState == ExoPlayer.STATE_BUFFERING){
                progressBar.visibility = View.VISIBLE
                tvMessage.text = getString(R.string.audio_loading)
                tvMessage.visibility = View.VISIBLE

            }else if(playbackState == ExoPlayer.STATE_READY){
                progressBar.visibility = View.INVISIBLE
                tvMessage.visibility = View.INVISIBLE
                tvMessage.text = getString(R.string.audio_playing)
            }

            if(playbackState == ExoPlayer.STATE_ENDED){
                progressBar.visibility = View.INVISIBLE
                tvMessage.visibility = View.INVISIBLE
                player?.seekTo(0); //if the video has finished playing, restart it.
            }
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            progressBar.visibility = View.INVISIBLE
            tvMessage.visibility = View.VISIBLE
            tvMessage.text = getString(R.string.audio_error)
        }
    }

}