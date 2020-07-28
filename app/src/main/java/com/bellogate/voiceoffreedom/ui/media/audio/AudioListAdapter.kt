package com.bellogate.voiceoffreedom.ui.media.audio

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bellogate.voiceoffreedom.R
import com.bellogate.voiceoffreedom.data.video.VideoRepository
import com.bellogate.voiceoffreedom.model.*
import com.bellogate.voiceoffreedom.util.getSimpleDateFormat
import com.bellogate.voiceoffreedom.util.showAlert
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception


/*** Reference: https://github.com/firebase/FirebaseUI-Android/blob/master/firestore/README.md ***/
class AudioListAdapter(options: FirestorePagingOptions<Audio>): FirestorePagingAdapter<Audio, ListItem>(options) {

    private var context: Context? = null
    private var user: User? = null
    private lateinit var audioItemClicked : (Audio)-> Unit
    private lateinit var uiState : (ListUIState)-> Unit

    constructor(context: Context,
                options: FirestorePagingOptions<Audio>,
                user: User?,
                uiState : (ListUIState)-> Unit,
                audioItemClicked : (Audio)-> Unit): this(options){

        this.context = context
        this.user = user
        this.uiState = uiState
        this.audioItemClicked = audioItemClicked
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItem {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.video_view_item, parent, false)
        return ListItem(context!!, view)
    }


    override fun onBindViewHolder(holder: ListItem, position: Int, audio: Audio) {

        if(audio != null){

            holder.ivThumbnail.setImageResource(R.drawable.ic_audiotrack)
            holder.shimmer.showShimmer(false)
            holder.shimmer.stopShimmer()
            holder.tvTitle.text = audio?.title
            holder.tvDuration.text = audio?.duration
            holder.tvDate.text = getSimpleDateFormat(
                audio!!.dateInMilliSeconds!!.toLong(),
                "dd-MMM-yyyy")//ie 30-APR-1994

            holder.itemLayout.setOnClickListener {
                audioItemClicked.invoke(audio!!)
            }

            //Admin privileges
            if(user != null && user!!.isAdmin) {
                holder.ivDeleteVideo.visibility = View.VISIBLE
                holder.ivDeleteVideo.setOnClickListener {
                    showAlert(context!!, "Delete", "Delete this video?"){
                        if(it){
                            holder.ivDeleteVideo.visibility = View.GONE
                            holder.shimmer.showShimmer(true)
                            holder.shimmer.startShimmer()

                            /*//delete the video:
                            VideoRepository(context!!).deleteVideo(video!!){ success, errorMessage ->
                                if(success){
                                    this.refresh()//refresh the list because an item has been removed.
                                    this.notifyDataSetChanged()
                                    Toast.makeText(context!!, "Deleted!", Toast.LENGTH_LONG).show()
                                }else{
                                    holder.ivDeleteVideo.visibility = View.VISIBLE
                                    holder.shimmer.stopShimmer()
                                    holder.shimmer.hideShimmer()
                                    Toast.makeText(context!!, "Try again $errorMessage", Toast.LENGTH_LONG).show()
                                }
                            }*/
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
        }else{
            holder.ivDeleteVideo.visibility = View.INVISIBLE
            uiState.invoke(ListUIState.NO_VIDEOS)
        }
    }


    override fun onLoadingStateChanged(state: LoadingState) {
        when (state) {

            LoadingState.LOADING_INITIAL ->{// this is the first method to be called.
                Log.e(AudioListAdapter::class.java.simpleName, "LOADING_INITIAL")
                uiState.invoke(ListUIState.FOUND)
            }

            LoadingState.LOADED ->{
                Log.e(AudioListAdapter::class.java.simpleName, "LOADING")
            }

            LoadingState.FINISHED ->{// this is the last method to be called after it has loaded all data from firestore
                //(pagination included)
                Log.e(AudioListAdapter::class.java.simpleName, "LOADING FINISHED")
            }

            LoadingState.LOADING_MORE ->{
                Log.e(AudioListAdapter::class.java.simpleName, "LOADING MORE")
            }

            LoadingState.ERROR ->{
                Log.e(AudioListAdapter::class.java.simpleName, "ERROR")
                uiState.invoke(ListUIState.ERROR)
            }
        }
    }
}