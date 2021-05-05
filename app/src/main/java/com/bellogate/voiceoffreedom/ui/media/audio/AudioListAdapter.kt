package com.bellogate.voiceoffreedom.ui.media.audio

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bellogate.voiceoffreedom.R
import com.bellogate.voiceoffreedom.data.audio.AudioRepository
import com.bellogate.voiceoffreedom.model.*
import com.bellogate.voiceoffreedom.util.getSimpleDateFormat
import com.bellogate.voiceoffreedom.util.showAlert
import com.bellogate.voiceoffreedom.util.showPopUpMenu
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState


/*** Reference: https://github.com/firebase/FirebaseUI-Android/blob/master/firestore/README.md ***/
class AudioListAdapter(options: FirestorePagingOptions<Audio>): FirestorePagingAdapter<Audio, ListItem>(options) {

    private var context: Context? = null
    private var user: User? = null
    private lateinit var audioItemClicked : (Audio)-> Unit
    private lateinit var uiState : (ListUIState)-> Unit
    private lateinit var downloadVideo:(audio: Audio)->Unit

    constructor(context: Context,
                options: FirestorePagingOptions<Audio>,
                user: User?,
                uiState : (ListUIState)-> Unit,
                audioItemClicked : (Audio)-> Unit,
                downloadVideo: (Audio)->Unit): this(options){

        this.context = context
        this.user = user
        this.uiState = uiState
        this.audioItemClicked = audioItemClicked
        this.downloadVideo = downloadVideo
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItem {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.list_item, parent, false)
        return ListItem(context!!, view)
    }


    override fun onBindViewHolder(holder: ListItem, position: Int, audio: Audio) {

        if(audio != null){


            //delete video
            fun deleteAudio(){
                showAlert(context!!, "Delete", "Delete this audio?"){
                    if(it){
                        holder.ivMenu.visibility = View.GONE
                        holder.shimmer.showShimmer(true)
                        holder.shimmer.startShimmer()

                        //delete the audio:
                        AudioRepository(context!!).deleteAudio(audio!!){ success, errorMessage ->
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


            //setup views
            holder.ivThumbnail.setImageResource(R.drawable.ic_audiotrack)
            holder.shimmer.showShimmer(false)
            holder.shimmer.stopShimmer()
            holder.tvTitle.text = audio?.title
            holder.tvDuration.text = audio?.duration
            holder.tvDate.text = getSimpleDateFormat(
                audio!!.dateInMilliSeconds!!.toLong(),
                "dd-MMM-yyyy")//ie 30-APR-1994


            //handle click events:
            holder.itemLayout.setOnClickListener {
                audioItemClicked.invoke(audio!!)
            }


            holder.ivMenu.setOnClickListener {
                val s = 10
                showPopUpMenu(R.menu.popup_menu, context!!, user, holder.ivMenu){
                    when (it.itemId) {
                        R.id.delete_item -> {
                            deleteAudio()
                        }
                        R.id.download_item -> {
                           downloadVideo.invoke(audio)
                        }
                    }
                }
            }

        }else{
            holder.ivMenu.visibility = View.INVISIBLE
            uiState.invoke(ListUIState.NO_VIDEOS)
        }
    }


    override fun onLoadingStateChanged(state: LoadingState) {
        when (state) {

            LoadingState.LOADING_INITIAL ->{// this is the first method to be called.
                //It does not mean that a audio has been found, it just means that the loading
                //process has began
                Log.e(AudioListAdapter::class.java.simpleName, "LOADING_INITIAL")
            }

            LoadingState.LOADED ->{//this means that at least one video item has been found
                Log.e(AudioListAdapter::class.java.simpleName, "LOADED")
                uiState.invoke(ListUIState.FOUND)
            }

            LoadingState.FINISHED ->{// this is the last method to be called after it has loaded all data from firestore
                //(pagination included)
                Log.e(AudioListAdapter::class.java.simpleName, "LOADING FINISHED")

                if(itemCount == 0){//this means that there is no data to show on the recycler view
                    //because no data was found in the server. So:
                    uiState.invoke(ListUIState.NO_VIDEOS)
                    Log.e(AudioListAdapter::class.java.simpleName, "NO DATA FOUND")
                }
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