package com.bellogate.voiceoffreedom.data.devotional

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.bellogate.voiceoffreedom.util.STOP_NOTIFICATION

class SyncDevotionalsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        if(intent.action == STOP_NOTIFICATION){
            SyncMultipleDevotionalsManager.uploadTask.cancel()
        }
    }
}
