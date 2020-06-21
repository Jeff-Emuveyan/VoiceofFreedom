package com.bellogate.voiceoffreedom.ui.media.video

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bellogate.voiceoffreedom.R
import com.bellogate.voiceoffreedom.data.video.VideoRepository
import com.bellogate.voiceoffreedom.model.User
import com.bellogate.voiceoffreedom.model.Video
import com.bellogate.voiceoffreedom.util.showAlert
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception

class VideoListAdapter(): RecyclerView.Adapter<VideoItem>() {

    private var context: Context? = null
    private var videoList: ArrayList<Video?>? = null
    private var user: User? = null

    constructor(context: Context, user: User?, lisOfVideos: ArrayList<Video?>?): this(){
        this.context = context
        this.videoList = lisOfVideos
        this.user = user
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
            holder.shimmer.showShimmer(true)
            holder.shimmer.startShimmer()
            Picasso.get().load(video?.thumbNailUrl).placeholder(R.drawable.ic_videocam)
                .error(R.drawable.ic_broken_image).into(holder.ivThumbnail, object : Callback {
                    override fun onError(e: Exception?) {
                        holder.shimmer.stopShimmer()
                        holder.shimmer.hideShimmer()
                        holder.ivThumbnail.setImageResource(R.drawable.ic_broken_image)
                    }
                    override fun onSuccess() {
                        holder.shimmer.stopShimmer()
                        holder.shimmer.hideShimmer()
                    }
                })

            holder.tvTitle.text = video?.title
            holder.tvDuration.text = video?.duration

            holder.itemLayout.setOnClickListener {

            }

            if(user != null && user!!.isAdmin) {
                holder.ivDeleteVideo.visibility = View.VISIBLE
                holder.ivDeleteVideo.setOnClickListener {
                    showAlert(context!!, "Delete", "Delete this video?"){
                        if(it){
                            holder.ivDeleteVideo.visibility = View.GONE
                            holder.shimmer.showShimmer(true)
                            holder.shimmer.startShimmer()

                            VideoRepository(context!!).deleteVideo(video!!){ success, errorMessage ->
                                if(success){
                                    videoList?.remove(video)
                                    this.notifyDataSetChanged()
                                    Toast.makeText(context!!, "Deleted!", Toast.LENGTH_LONG).show()
                                }else{
                                    holder.ivDeleteVideo.visibility = View.VISIBLE
                                    holder.shimmer.stopShimmer()
                                    holder.shimmer.hideShimmer()
                                    Toast.makeText(context!!, "Try again", Toast.LENGTH_LONG).show()
                                }
                            }
                        }else{
                            holder.ivDeleteVideo.visibility = View.VISIBLE
                            holder.shimmer.stopShimmer()
                            holder.shimmer.hideShimmer()
                            Toast.makeText(context!!, "Try again", Toast.LENGTH_LONG).show()
                        }
                    }

                }
            }else{
                holder.ivDeleteVideo.visibility = View.INVISIBLE
            }
        }
    }



}