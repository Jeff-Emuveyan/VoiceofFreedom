package com.bellogate.voiceoffreedom.ui.home

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.bellogate.voiceoffreedom.data.datasource.network.NetworkHelper
import com.bellogate.voiceoffreedom.model.Event
import com.bellogate.voiceoffreedom.ui.BaseViewModel
import com.bellogate.voiceoffreedom.util.EVENTS
import com.bellogate.voiceoffreedom.util.VIDEOS
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class HomeViewModel : BaseViewModel() {


    /** Fetches an event ***/
    val event = MutableLiveData<Pair<NetworkState, Event?>>()
    fun fetchLatestEvent() {
        NetworkHelper.getLatestEvent { networkState, event ->
            this.event.value = networkState to event
        }
    }

    fun syncImage(uri: Uri, response:(Boolean, String?)-> Unit) {

        val storage = Firebase.storage
        val reference = storage.reference
        val imageRef: StorageReference = reference.child("$EVENTS/1")

        imageRef.putFile(uri).addOnCompleteListener {
            if(it.isSuccessful){
                imageRef.downloadUrl.addOnSuccessListener { imageRefUri ->
                    val event = Event(imageRefUri.toString())
                    NetworkHelper.syncEvent(event){ success, message ->
                        response.invoke(success, message)
                    }
                }
            }else{
                response.invoke(false, "Failed")
            }
        }
    }
}