package com.bellogate.voiceoffreedom.ui.media.video.add

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bellogate.voiceoffreedom.data.video.VideoRepository
import com.bellogate.voiceoffreedom.util.NO_VALUE_SET
import com.bellogate.voiceoffreedom.util.TotalFileSize
import com.bellogate.voiceoffreedom.util.UUID
import java.util.*


class AddVideoViewModel : ViewModel() {

    var values: MutableLiveData<Pair<UUID, Long>> = MutableLiveData<Pair<UUID, Long>>().apply {
        value = null
    }


    fun uploadVideo(context: Context, videoTitle: String, videoUri: Uri) {
        val dateInMilliSeconds = System.currentTimeMillis().toString()
        VideoRepository(context).uploadVideo(videoUri, videoTitle, dateInMilliSeconds){
            val uuid = it.first
            val fileSize = it.second
            values.value = uuid to fileSize
        }
    }


    /*** Saves the uuid and file size of the current upload work so that it can be retrieved when
     * the fragment restarts.
     */
    fun saveCurrentUploadWorkInformation(context: Context,uuid: UUID, totalByteCount: Long){
        val sharedPref: SharedPreferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString(UUID, uuid.toString())
        editor.putLong(TotalFileSize, totalByteCount)
        editor.apply()

        Log.e("AddVideoViewModel", "Saving upload work info::: uuid: $uuid, totalByteCount: $totalByteCount")
    }


    /*** Returns the uuid and file size of the current upload work so that it can be used to indicate
     * that a work is already in progress when the fragment restarts.
     */
    fun getCurrentUploadWorkInformation(context: Context): MutableLiveData<Pair<UUID, Long>>{
        val sharedPref: SharedPreferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE)

        val id = sharedPref.getString(UUID, NO_VALUE_SET.toString())
        val totalByteCount = sharedPref.getLong(TotalFileSize, NO_VALUE_SET)

        return if(id != null && id != NO_VALUE_SET.toString() &&
            totalByteCount != null && totalByteCount != NO_VALUE_SET){

            val uuid = java.util.UUID.fromString(id)

            Log.e("AddVideoViewModel", "Fetching existing upload work::: uuid: $uuid, totalByteCount: $totalByteCount")

            val mData = MutableLiveData<Pair<UUID, Long>>().apply {
                value = uuid to totalByteCount
                Log.e("AddVideoViewModel", "Saved workInfo::: uuid: $uuid, totalByteCount: $totalByteCount")
            }
            mData

        }else{
            MutableLiveData<Pair<UUID, Long>>().apply {
                value = null
                Log.e("AddVideoViewModel", "Saved workInfo::: uuid: null, totalByteCount: null")
            }
        }
    }



}
