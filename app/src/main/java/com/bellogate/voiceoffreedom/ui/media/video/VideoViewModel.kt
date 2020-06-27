package com.bellogate.voiceoffreedom.ui.media.video

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.bellogate.voiceoffreedom.data.video.VideoRepository
import com.bellogate.voiceoffreedom.model.Video
import com.bellogate.voiceoffreedom.ui.BaseViewModel
import com.bellogate.voiceoffreedom.util.DATE_IN_MILLISECONDS
import com.bellogate.voiceoffreedom.util.VIDEOS
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class VideoViewModel : BaseViewModel() {

    fun options(lifecycleOwner: LifecycleOwner): FirestorePagingOptions<Video> {
        var db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val baseQuery: Query =
            db.collection(VIDEOS).orderBy(DATE_IN_MILLISECONDS, Query.Direction.DESCENDING)

        val config: PagedList.Config = PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setPrefetchDistance(10)
            .setPageSize(1)
            .build()

        return FirestorePagingOptions.Builder<Video>()
                .setLifecycleOwner(lifecycleOwner)
                .setQuery(baseQuery, config, Video::class.java)
                .build()
}

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

}
