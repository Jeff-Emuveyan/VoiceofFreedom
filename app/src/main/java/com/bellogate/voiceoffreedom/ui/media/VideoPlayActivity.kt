package com.bellogate.voiceoffreedom.ui.media

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bellogate.voiceoffreedom.R
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Log
import kotlinx.android.synthetic.main.activity_video_play.*

class VideoPlayActivity : AppCompatActivity() {

    private var exoPlayer: SimpleExoPlayer? = null
    lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_play)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Loading...");
        progressDialog.show(); // will display the Progress Dialog.

        val extras = intent.extras
        if (extras != null) {
            var data = extras!!.getString("urll") // retrieve the data using keyName

            playVid(data.toString())

        }
    }

    override fun onStop() {
        super.onStop()
        exoPlayer?.stop()
    }


    fun playVid(url:String){

        try {
            val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter.Builder(this).build()
            val trackSelector: TrackSelector =
                DefaultTrackSelector(AdaptiveTrackSelection.Factory(bandwidthMeter))
            exoPlayer = ExoPlayerFactory.newSimpleInstance(this) as SimpleExoPlayer
            val video = Uri.parse(url)
            val dataSourceFactory =
                DefaultHttpDataSourceFactory("video")
            val extractorsFactory: ExtractorsFactory = DefaultExtractorsFactory()
            val mediaSource: MediaSource =
                ExtractorMediaSource(video, dataSourceFactory, extractorsFactory, null, null)
            exo_player_video_view.player = exoPlayer
            exoPlayer!!.prepare(mediaSource); //buffer videos
            exoPlayer!!.playWhenReady = true
            exo_player_video_view.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT //Aspect ratio
            if (exoPlayer!!.isPlaying){progressDialog.dismiss()}
            exoPlayer!!.addListener(object : Player.EventListener {

                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playWhenReady && playbackState==3){
                    progressDialog.dismiss()
                }
                }
            })


//            playerView.setOnClickListener(new View.OnClickListener()
//            {
//                @Override
//                public void onClick(final View v)
//                {
//                    if (exoPlayer.getBufferedPosition() == 0.0){
//                        exoPlayer.prepare(mediaSource);
//
//                    }
//                }
//            });
        } catch (e: Exception) {
            Log.e(
                "ViewHolder2",
                "exoplayer error$e"
            )
        }

    }

}