package com.bellogate.voiceoffreedom.ui.media.video

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bellogate.voiceoffreedom.R
import com.bellogate.voiceoffreedom.model.Video
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception

class VideoListAdapter(): RecyclerView.Adapter<VideoItem>() {

    var context: Context? = null
    var videoList: ArrayList<Video?>? = null

    constructor(context: Context, lisOfVideos: ArrayList<Video?>?): this(){
        this.context = context
        this.videoList = lisOfVideos
    }

    override fun getItemCount(): Int {
        return videoList?.size ?: 0
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoItem {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.video_view_item, parent, false)
        return VideoItem(context!!, view)
    }


    override fun onBindViewHolder(holder: VideoItem, position: Int) {

        if(videoList != null && videoList!!.size > 0){
            val video = videoList!![position]

            //load the thumbnail:
            Picasso.get().load(video?.thumbNailUrl).placeholder(R.drawable.ic_videocam)
                .error(R.drawable.ic_broken_image).into(holder.ivThumbnail)

            holder.tvTitle.text = video?.title
            holder.tvDuration.text = video?.duration
        }

    }
}