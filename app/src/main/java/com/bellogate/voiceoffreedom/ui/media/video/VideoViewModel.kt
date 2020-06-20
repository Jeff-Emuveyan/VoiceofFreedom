package com.bellogate.voiceoffreedom.ui.media.video

import android.R
import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.bellogate.voiceoffreedom.data.video.VideoRepository
import com.bellogate.voiceoffreedom.model.Video
import com.bellogate.voiceoffreedom.ui.BaseViewModel
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory


class VideoViewModel : BaseViewModel() {

    fun fetchVideos(context: Context, response:(VideoUIState, ArrayList<Video?>?)-> Unit){
        VideoRepository(context).fetchVideos{ state, list ->
            response.invoke(state, list)
        }
    }


    fun getMediaSource(context: Context, videoLink: String): MediaSource {
        val uri = Uri.parse(videoLink)
        return buildMediaSource(context, uri)
    }

    private fun buildMediaSource(context: Context, uri: Uri): MediaSource {
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(context, "exoplayer")
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }


    /*** Gets the title of the first video on the list **/
    fun getFirstTitle(list: ArrayList<Video?>?): String{
        return if(!list.isNullOrEmpty()){
            val video = list.first()
            video?.title!!
        }else{
            "Title"
        }
    }

}
