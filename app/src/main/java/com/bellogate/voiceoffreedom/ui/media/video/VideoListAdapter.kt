package com.bellogate.voiceoffreedom.ui.media.video

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bellogate.voiceoffreedom.R
import com.bellogate.voiceoffreedom.data.video.VideoRepository
import com.bellogate.voiceoffreedom.model.User
import com.bellogate.voiceoffreedom.model.Video
import com.bellogate.voiceoffreedom.model.ListItem
import com.bellogate.voiceoffreedom.model.ListUIState
import com.bellogate.voiceoffreedom.util.downloadFile
import com.bellogate.voiceoffreedom.util.getSimpleDateFormat
import com.bellogate.voiceoffreedom.util.showAlert
import com.bellogate.voiceoffreedom.util.showMediaPopUpMenu
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception


/*** Reference: https://github.com/firebase/FirebaseUI-Android/blob/master/firestore/README.md ***/
class VideoListAdapter(options: FirestorePagingOptions<Video>): FirestorePagingAdapter<Video, ListItem>(options) {

    private var context: Context? = null
    private var user: User? = null
    private lateinit var videoItemClicked : (Video)-> Unit
    private lateinit var firstVideoReady : (Video)-> Unit
    private lateinit var uiState : (ListUIState)-> Unit

    constructor(context: Context,
                options: FirestorePagingOptions<Video>,
                user: User?,
                uiState : (ListUIState)-> Unit,
                firstVideoReady : (Video)-> Unit,
                videoItemClicked : (Video)-> Unit): this(options){

        this.context = context
        this.user = user
        this.uiState = uiState
        this.firstVideoReady = firstVideoReady
        this.videoItemClicked = videoItemClicked
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItem {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.list_item, parent, false)
        return ListItem(context!!, view)
    }


    override fun onBindViewHolder(holder: ListItem, position: Int, video: Video) {

        if(video != null){

            //check to automatically play the first video:
            if(position == 0) {
                firstVideoReady.invoke(video)
            }

            //delete video
            fun deleteVideo(){
                showAlert(context!!, "Delete", "Delete this video?"){
                    if(it){
                        holder.ivMenu.visibility = View.GONE
                        holder.shimmer.showShimmer(true)
                        holder.shimmer.startShimmer()

                        //delete the video:
                        VideoRepository(context!!).deleteVideo(video!!){ success, errorMessage ->
                            if(success){
                                this.refresh()//refresh the list because an item has been removed.
                                this.notifyDataSetChanged()
                                Toast.makeText(context!!, "Deleted!", Toast.LENGTH_LONG).show()
                            }else{
                                holder.ivMenu.visibility = View.VISIBLE
                                holder.shimmer.stopShimmer()
                                holder.shimmer.hideShimmer()
                                Toast.makeText(context!!, "Try again $errorMessage", Toast.LENGTH_LONG).show()
                            }
                        }
                    }else{
                        holder.ivMenu.visibility = View.VISIBLE
                        holder.shimmer.stopShimmer()
                        holder.shimmer.hideShimmer()
                        Toast.makeText(context!!, "Try again", Toast.LENGTH_LONG).show()
                    }
                }
            }

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


            //set the texts:
            holder.tvTitle.text = video?.title
            holder.tvDuration.text = video?.duration
            holder.tvDate.text = getSimpleDateFormat(
                video!!.dateInMilliSeconds!!.toLong(),
                "dd-MMM-yyyy")//ie 30-APR-1994


            //handle click events:
            holder.itemLayout.setOnClickListener {
                videoItemClicked.invoke(video!!)
            }


            holder.ivMenu.setOnClickListener {
                showMediaPopUpMenu(context!!, user, holder.ivMenu){
                    when (it.itemId) {
                        R.id.delete_item -> {
                            deleteVideo()
                        }
                        R.id.download_item -> {
                            downloadVideo(video.videoUrl)
                        }
                    }
                }
            }
        }else{
            holder.ivMenu.visibility = View.INVISIBLE
            uiState.invoke(ListUIState.NO_VIDEOS)
        }
    }



    private fun downloadVideo(videoUrl: String?) = downloadFile(context!!, videoUrl!!)


    override fun onLoadingStateChanged(state: LoadingState) {
        when (state) {

            LoadingState.LOADING_INITIAL ->{// this is the first method to be called.
                Log.e(VideoListAdapter::class.java.simpleName, "LOADING_INITIAL")
                uiState.invoke(ListUIState.FOUND)
            }

            LoadingState.LOADED ->{
                Log.e(VideoListAdapter::class.java.simpleName, "LOADING")
            }

            LoadingState.FINISHED ->{// this is the last method to be called after it has loaded all data from firestore
                //(pagination included)
                Log.e(VideoListAdapter::class.java.simpleName, "LOADING FINISHED")
            }

            LoadingState.LOADING_MORE ->{
                Log.e(VideoListAdapter::class.java.simpleName, "LOADING MORE")
            }

            LoadingState.ERROR ->{
                Log.e(VideoListAdapter::class.java.simpleName, "ERROR")
                uiState.invoke(ListUIState.ERROR)
            }
        }
    }
}