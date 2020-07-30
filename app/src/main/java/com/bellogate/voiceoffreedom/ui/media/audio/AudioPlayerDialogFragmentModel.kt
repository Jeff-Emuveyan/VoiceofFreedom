package com.bellogate.voiceoffreedom.ui.media.audio

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory

class AudioPlayerDialogFragmentModel: ViewModel() {

    fun getMediaSource(context: Context, videoLink: String): MediaSource {
        val uri = Uri.parse(videoLink)
        return buildMediaSource(context, uri)
    }

    private fun buildMediaSource(context: Context, uri: Uri): MediaSource {
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(context, "exoplayer")
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }
}