package com.bellogate.voiceoffreedom.data.video

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.*
import com.bellogate.voiceoffreedom.data.BaseRepository
import com.bellogate.voiceoffreedom.data.datasource.network.NetworkHelper
import com.bellogate.voiceoffreedom.model.Video
import com.bellogate.voiceoffreedom.model.ListUIState
import com.bellogate.voiceoffreedom.util.FileUtil
import java.util.*

class VideoRepository(context: Context): BaseRepository(context) {

    fun uploadVideo(videoUri: Uri,
                    videoTitle: String,
                    dateInMilliSeconds: String,
                    values: (Pair<UUID, Long>) -> Unit){

        //start the Worker to upload the video:
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val uploadWorkRequest: OneTimeWorkRequest = OneTimeWorkRequestBuilder<SyncVideoManager>()
            .addTag("syncVideo")
            .setInputData(workDataOf(
                "videoTitle" to videoTitle,
                "dateInMilliSeconds" to dateInMilliSeconds
            ))
            .setConstraints(constraints).build()

        //"setInputData()" can only hold primitive types in its Pairs, so we have to set the uri like so:
        SyncVideoManager.videoUri = videoUri

        WorkManager
            .getInstance(context)
            .enqueueUniqueWork("syncVideo", ExistingWorkPolicy.REPLACE, uploadWorkRequest)

        val fileSize = FileUtil.from(context, videoUri).length()

        values.invoke(uploadWorkRequest.id to fileSize)

        Log.e(SyncVideoManager::class.java.simpleName, "id: ${uploadWorkRequest.id}")
    }


    fun fetchVideos(response:(ListUIState, ArrayList<Video?>?)-> Unit){
        NetworkHelper.fetchVideos{ state, list ->
            response.invoke(state, list)
        }
    }


    /*** Deletes the video object in firestore ***/
    fun deleteVideo(video: Video, response:(Boolean, String?)-> Unit){
        NetworkHelper.deleteVideo(video){ aSuccess, aErrorMessage ->
            if(aSuccess){
                NetworkHelper.deleteVideoFile(video) { bSuccess, bErrorMessage ->
                    if(bSuccess){
                        NetworkHelper.deleteVideoThumbnail(video){ cSuccess, cErrorMessage ->
                            if(cSuccess){
                                response.invoke(true, null)
                            }else{
                                response.invoke(false, cErrorMessage)
                            }
                        }
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