package com.bellogate.voiceoffreedom.ui.media.audio

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.bellogate.voiceoffreedom.data.audio.AudioRepository
import com.bellogate.voiceoffreedom.ui.BaseViewModel

class AudioViewModel : BaseViewModel() {

    fun uploadAudio(context: Context, uri: Uri) {
        val dateInMilliSeconds = System.currentTimeMillis().toString()
        AudioRepository(context).uploadAudio(context, dateInMilliSeconds, uri)
    }

}
