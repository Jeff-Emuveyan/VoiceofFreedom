package com.bellogate.voiceoffreedom.data.video

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bellogate.voiceoffreedom.data.datasource.network.NetworkHelper
import com.bellogate.voiceoffreedom.model.Video
import com.bellogate.voiceoffreedom.util.THUMBNAILS
import com.bellogate.voiceoffreedom.util.VIDEOS
import com.bellogate.voiceoffreedom.util.toBytes
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
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

        //upload thumbnail first:
        thumbnailUploadTask = thumbnailRef.putBytes(thumbnail).addOnSuccessListener {

            //upload video:
            NetworkHelper.videoUploadTask = videoRef.putFile(videoUri).addOnSuccessListener {

                videoRef.downloadUrl.addOnSuccessListener { videoRefUri ->
                    thumbnailRef.downloadUrl.addOnSuccessListener { thumbnailUri ->

                        val video = Video(
                            videoTitle,
                            thumbnailUri.toString(),
                            videoRefUri.toString(),
                            videoDuration,
                            dateInMilliSeconds
                        )

                        NetworkHelper.syncVideo(video) { success, errorMessage ->
                            if (success) {
                                Log.e(SyncVideoManager::class.java.simpleName, "Success!")
                            }
                        }
                    }
                }
            }.addOnFailureListener {
                Log.e(SyncVideoManager::class.java.simpleName, "Failed!")
            }.addOnProgressListener {

                Log.e(SyncVideoManager::class.java.simpleName, "Size: ${it.totalByteCount}, " +
                            "Uploaded: ${it.bytesTransferred}")

                setForegroundAsync(createForegroundInfo(it.totalByteCount.toInt(), it.bytesTransferred.toInt())!!)
            }
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


    private fun createForegroundInfo(max: Int, progress: Int): ForegroundInfo? { // Build a notification using bytesRead and contentLength
        val context = applicationContext
        val id = "com.bellogate.caliphate"
        val title = "Uploading..."
        val cancel = "Stop"
        // This PendingIntent can be used to cancel the worker
        val intent = WorkManager.getInstance(context)
            .createCancelPendingIntent(getId())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(appContext)
        }
        val notification: Notification = NotificationCompat.Builder(context, id)
            .setContentTitle(title)
            .setTicker(title)
            .setSmallIcon(R.drawable.alert_light_frame)
            .setProgress(max, progress, false)
            .setOngoing(true) // Add the cancel action to the notification which can
            // be used to cancel the worker
            .addAction(R.drawable.ic_delete, cancel, intent)
            .build()
        return ForegroundInfo(33, notification)
    }


    private fun createNotificationChannel(context: Context) { //If you don't call this method, you notifications will only show on older versions of android phones.
        val CHANNEL_NAME = "voice_of_freedom"
        val CHANNEL_ID = "com.bellogate.caliphate"
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT// IMPORTANCE_LOW means this
            //notification will not play sound. Change it to IMPORTANCE_DEFAULT if you want sound.
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
            channel.description = "voice_of_freedom"
            channel.enableVibration(true)
            channel.enableLights(true)
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = context.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
            //Toast.makeText(SplashActivity.this, "New Phone", Toast.LENGTH_LONG).show();
        }
    }
}