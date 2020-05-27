package com.bellogate.voiceoffreedom.data

import android.content.Context
import com.bellogate.voiceoffreedom.data.BaseRepository
import com.bellogate.voiceoffreedom.data.datasource.network.NetworkHelper
import com.bellogate.voiceoffreedom.model.Key

class KeyRepository(context: Context) : BaseRepository(context){

    fun fetchKey(response:(success: Boolean, key: Key?)-> Unit){
        NetworkHelper.getKey { success, key ->
            response.invoke(success, key)
        }
    }
}