package com.bellogate.voiceoffreedom.data.audio

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.bellogate.voiceoffreedom.data.datasource.network.NetworkHelper
import com.bellogate.voiceoffreedom.data.video.SyncVideoManager
import com.bellogate.voiceoffreedom.model.Audio
import com.bellogate.voiceoffreedom.util.AUDIOS
import com.bellogate.voiceoffreedom.util.Progress
import com.bellogate.voiceoffreedom.util.createForegroundInfo
import com.bellogate.voiceoffreedom.util.showNotification
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SyncAudioManager(private val appContext: Context, workerParams: WorkerParameters): CoroutineWorker(appContext, workerParams) {

    private var audioUploadTask: UploadTask.TaskSnapshot? = null

    companion object{
        lateinit var audioUri: Uri
    }


    override suspend fun doWork(): Result {

        val audioTitle = inputData.getString("audioTitle")
        val dateInMilliSeconds = inputData.getString("dateInMilliSeconds")
        val duration = inputData.getString("duration")

        Log.e(SyncVideoManager::class.java.simpleName, "Worker starts....")

        uploadAudioFile(audioTitle, dateInMilliSeconds, duration, audioUri)

        return Result.success()
    }


    private suspend fun uploadAudioFile(audioTitle: String?, dateInMilliSeconds: String?, duration: String?, audioUri: Uri) {

        val storage = Firebase.storage
        val reference = storage.reference
        val audioRef: StorageReference = reference.child("$AUDIOS/${dateInMilliSeconds}")

        //upload audio:
        audioUploadTask = audioRef.putFile(audioUri).addOnProgressListener {
            Log.e(SyncVideoManager::class.java.simpleName, "Size: ${it.totalByteCount}, " +
                    "Uploaded: ${it.bytesTransferred}")

            //emit the progress:
            val progress = workDataOf(Progress to it.bytesTransferred)

            val coroutineScope = CoroutineScope(coroutineContext + Job())
            coroutineScope.launch(Dispatchers.Default) {
                setProgress(progress)//setProgress can only publish one data throughout. So we
                //can't do setProgress(total) too.

                setForeground(createForegroundInfo(appContext, id, it.totalByteCount, it.bytesTransferred))
                Log.e(SyncVideoManager::class.java.simpleName, "setProgress called")
            }
        }.await()


        Log.e(SyncVideoManager::class.java.simpleName, "Video upload should have ended")
        if(audioUploadTask!!.task.isSuccessful){
            showNotification(appContext, "Upload successful", "")
            Log.e(SyncVideoManager::class.java.simpleName, "Audio uploaded successfully")

            var audiolUri = audioRef.downloadUrl.await()

            val audio = Audio(audioTitle, audiolUri.toString(), duration, dateInMilliSeconds)

            //Write the audio object:
            NetworkHelper.db.collection(AUDIOS).document(audio.dateInMilliSeconds!!).set(audio).await()

        }else{
            showNotification(appContext, "Upload failed, try again", "")
            Log.e(SyncVideoManager::class.java.simpleName, "Audio uploaded has failed")
        }
    }
}