package com.bellogate.voiceoffreedom.ui.devotional.add

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import androidx.work.*
import com.bellogate.voiceoffreedom.data.devotional.SyncMultipleDevotionalsManager
import com.bellogate.voiceoffreedom.ui.BaseViewModel
import com.bellogate.voiceoffreedom.util.centerToast
import com.bellogate.voiceoffreedom.util.showAlert

class AddDevotionalViewModel : BaseViewModel() {


    /** For this version, we only allow the user to add a maximum number of 4 collectors.
     * This is so that we won't need to manage the state of the recycler items when the user has
     * scrolled down a long list.
     */
    fun maximumNumberOfCollectorsReached(currentNumber: Int): Boolean{
        return (currentNumber == 4)
    }

    fun validateAndSyncInput(context: Context, validateInput: (Boolean)-> Unit){
        SyncMultipleDevotionalsManager.validateInput(context,validateInput = {
            if(it){
                syncMultipleDevotionals(context)
            }
            validateInput.invoke(it)
        })
    }

    private fun syncMultipleDevotionals(context: Context){
        //start the WorkManager:
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val uploadWorkRequest: OneTimeWorkRequest = OneTimeWorkRequestBuilder<SyncMultipleDevotionalsManager>()
            .setConstraints(constraints).build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork("syncDevotionals", ExistingWorkPolicy.APPEND, uploadWorkRequest)
    }
}
