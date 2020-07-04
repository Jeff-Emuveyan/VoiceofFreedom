package com.bellogate.voiceoffreedom.ui.home

import androidx.lifecycle.MutableLiveData
import com.bellogate.voiceoffreedom.data.datasource.network.NetworkHelper
import com.bellogate.voiceoffreedom.model.Event
import com.bellogate.voiceoffreedom.ui.BaseViewModel

class HomeViewModel : BaseViewModel() {


    /** Fetches an event ***/
    val event = MutableLiveData<Pair<NetworkState, Event?>>()
    fun fetchLatestEvent() {
        NetworkHelper.getLatestEvent { networkState, event ->
            this.event.value = networkState to event
        }
    }
}