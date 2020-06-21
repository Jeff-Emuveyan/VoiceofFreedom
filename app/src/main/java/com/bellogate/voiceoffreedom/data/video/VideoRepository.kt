package com.bellogate.voiceoffreedom.data.video

import android.content.Context
import android.net.Uri
import androidx.work.*
import com.bellogate.voiceoffreedom.data.BaseRepository
import com.bellogate.voiceoffreedom.data.datasource.network.NetworkHelper
import com.bellogate.voiceoffreedom.model.Video
import com.bellogate.voiceoffreedom.ui.media.video.VideoUIState

class VideoRepository(context: Context): BaseRepository(context) {

    fun uploadVideo(videoUri: Uri, videoTitle: String, dateInMilliSeconds: String) {
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
            .enqueueUniqueWork("syncVideo", ExistingWorkPolicy.APPEND, uploadWorkRequest)
    }


    fun fetchVideos(response:(VideoUIState, ArrayList<Video?>?)-> Unit){
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