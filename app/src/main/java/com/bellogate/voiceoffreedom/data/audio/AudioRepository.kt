package com.bellogate.voiceoffreedom.data.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.work.*
import com.bellogate.voiceoffreedom.data.BaseRepository
import com.bellogate.voiceoffreedom.data.datasource.network.NetworkHelper
import com.bellogate.voiceoffreedom.data.video.SyncVideoManager
import com.bellogate.voiceoffreedom.model.Audio
import com.bellogate.voiceoffreedom.util.getDuration
import java.io.File
import java.util.concurrent.TimeUnit

class AudioRepository(context: Context): BaseRepository(context) {

    fun uploadAudio(context: Context, dateInMilliSeconds: String, uri: Uri) {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val uploadWorkRequest: OneTimeWorkRequest = OneTimeWorkRequestBuilder<SyncAudioManager>()
            .addTag("syncAudio")
            .setInputData(
                workDataOf(
                    "audioTitle" to File(uri.path).name,
                    "dateInMilliSeconds" to dateInMilliSeconds,
                    "duration" to getDuration(context, uri)
                )
            )
            .setConstraints(constraints).build()

        //"setInputData()" can only hold primitive types in its Pairs, so we have to set the uri like so:
        SyncAudioManager.audioUri = uri

        WorkManager
            .getInstance(context)
            .enqueueUniqueWork("syncAudio", ExistingWorkPolicy.REPLACE, uploadWorkRequest)
    }


    /*** Deletes the audio object in FireStore and then audio file in storage ***/
    fun deleteAudio(audio: Audio, response:(Boolean, String?)-> Unit){
        NetworkHelper.deleteAudio(audio) { aSuccess, aErrorMessage ->
            if(aSuccess){
                NetworkHelper.deleteAudioFile(audio) { bSuccess, bErrorMessage ->
                    if(bSuccess){
                        response.invoke(true, null)
                    }else{
                        response.invoke(false, bErrorMessage)
                    }
                }
            }else{
                response.invoke(false, aErrorMessage)
            }
        }
    }
}