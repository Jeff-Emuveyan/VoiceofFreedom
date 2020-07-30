package com.bellogate.voiceoffreedom.ui.media.audio

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagedList
import com.bellogate.voiceoffreedom.data.audio.AudioRepository
import com.bellogate.voiceoffreedom.model.Audio
import com.bellogate.voiceoffreedom.ui.BaseViewModel
import com.bellogate.voiceoffreedom.util.AUDIOS
import com.bellogate.voiceoffreedom.util.DATE_IN_MILLISECONDS
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class AudioViewModel : BaseViewModel() {

    fun options(lifecycleOwner: LifecycleOwner): FirestorePagingOptions<Audio> {
        var db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val baseQuery: Query =
            db.collection(AUDIOS).orderBy(DATE_IN_MILLISECONDS, Query.Direction.DESCENDING)

        val config: PagedList.Config = PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setPrefetchDistance(10)
            .setPageSize(1)
            .build()

        return FirestorePagingOptions.Builder<Audio>()
            .setLifecycleOwner(lifecycleOwner)
            .setQuery(baseQuery, config, Audio::class.java)
            .build()
    }

    fun uploadAudio(context: Context, uri: Uri) {
        val dateInMilliSeconds = System.currentTimeMillis().toString()
        AudioRepository(context).uploadAudio(context, dateInMilliSeconds, uri)
    }


    fun playAudio(fragmentManager: FragmentManager, audio: Audio){
        AudioPlayerDialogFragment(audio).show(fragmentManager, "")

    }

}
