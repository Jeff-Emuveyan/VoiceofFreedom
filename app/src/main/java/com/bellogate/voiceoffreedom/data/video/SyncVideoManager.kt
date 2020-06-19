package com.bellogate.voiceoffreedom.data.video

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bellogate.voiceoffreedom.data.datasource.network.NetworkHelper
import com.bellogate.voiceoffreedom.model.Video
import com.bellogate.voiceoffreedom.util.DEVOTIONALS
import com.bellogate.voiceoffreedom.util.THUMBNAILS
import com.bellogate.voiceoffreedom.util.VIDEOS
import com.bellogate.voiceoffreedom.util.toBytes
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.util.concurrent.TimeUnit

class SyncVideoManager (private val appContext: Context, workerParams: WorkerParameters): Worker(appContext, workerParams) {

    var  thumbnailUploadTask: StorageTask<UploadTask.TaskSnapshot>? = null

    companion object{
        lateinit var videoUri: Uri
    }
    override fun doWork(): Result {

        val videoTitle = inputData.getString("videoTitle")
        val dateInMilliSeconds = inputData.getString("dateInMilliSeconds")

        //get the thumbnail:
        val mMMR = MediaMetadataRetriever()
        mMMR.setDataSource(appContext, videoUri)
        var thumbnail = mMMR.frameAtTime

        //get the duration:
        val videoDuration = getDuration(appContext, videoUri)

        uploadVideoFile(videoTitle!!, videoDuration, videoUri, thumbnail.toBytes(), dateInMilliSeconds!!)

        return Result.success()
    }


    /** Used to upload a video  file **/
    private fun uploadVideoFile(videoTitle: String,
                                videoDuration: String,
                                videoUri: Uri,
                                thumbnail: ByteArray,
                                dateInMilliSeconds: String){

        val storage = Firebase.storage
        val reference = storage.reference
        val videoRef: StorageReference = reference.child("$VIDEOS/${dateInMilliSeconds}")
        val thumbnailRef: StorageReference = reference.child("$THUMBNAILS/${dateInMilliSeconds}")

        //upload video:
        NetworkHelper.videoUploadTask = videoRef.putFile(videoUri).addOnSuccessListener {

            //upload thumbnail:
            thumbnailUploadTask = thumbnailRef.putFile(videoUri).addOnSuccessListener {

                videoRef.downloadUrl.addOnSuccessListener {videoRefUri ->
                    thumbnailRef.downloadUrl.addOnSuccessListener {thumbnailUri ->

                        val video = Video(
                            videoTitle,
                            thumbnailUri.toString(),
                            videoRefUri.toString(),
                            videoDuration,
                            dateInMilliSeconds)

                        NetworkHelper.syncVideo(video){success, errorMessage ->
                            if(success) {
                                Log.e(SyncVideoManager::class.java.simpleName, "Success!")
                            }
                        }
                    }
                }
            }

        }.addOnFailureListener {
            Log.e(SyncVideoManager::class.java.simpleName,"Failed!")
        }.addOnProgressListener {
            Log.e(SyncVideoManager::class.java.simpleName,"Size: ${it.totalByteCount}, " +
                    "Uploaded: ${it.bytesTransferred}")
        }
    }


    private fun getDuration(context: Context, videoUri: Uri): String {

        val mp: MediaPlayer = MediaPlayer.create(context, videoUri)
        val duration = mp.duration
        mp.release()

        return  String.format("%d min, %d sec",
            TimeUnit.MILLISECONDS.toMinutes(duration.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration.toLong())))
    }
}