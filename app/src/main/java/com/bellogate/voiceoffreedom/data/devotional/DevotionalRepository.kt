package com.bellogate.voiceoffreedom.data.devotional

import android.content.Context
import com.bellogate.voiceoffreedom.data.BaseRepository
import com.bellogate.voiceoffreedom.data.datasource.network.NetworkHelper
import com.bellogate.voiceoffreedom.model.Devotional
import com.bellogate.voiceoffreedom.ui.devotional.util.UIState

class DevotionalRepository(context: Context) : BaseRepository(context) {

    fun getDevotionalByDate(dateToFind: String, response: (UIState, Devotional?)-> Unit){
        NetworkHelper.getDevotionalByDate(dateToFind){ uiState, devotional ->
            response.invoke(uiState, devotional)
        }
    }
}