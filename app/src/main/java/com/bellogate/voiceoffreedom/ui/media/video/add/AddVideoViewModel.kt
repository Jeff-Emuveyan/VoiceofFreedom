package com.bellogate.voiceoffreedom.ui.media.video.add

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bellogate.voiceoffreedom.data.video.VideoRepository
import java.util.*

class AddVideoViewModel : ViewModel() {

    val id: MutableLiveData<UUID> = MutableLiveData<UUID>().apply {
        value = null
    }


    fun uploadVideo(context: Context, videoTitle: String, videoUri: Uri) {
        val dateInMilliSeconds = System.currentTimeMillis().toString()
        VideoRepository(context).uploadVideo(videoUri, videoTitle, dateInMilliSeconds){
            id.value = it
        }
    }

}
