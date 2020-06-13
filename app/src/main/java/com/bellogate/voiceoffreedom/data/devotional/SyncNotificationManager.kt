package com.bellogate.voiceoffreedom.data.devotional

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.Keep
import androidx.core.app.NotificationCompat
import com.bellogate.voiceoffreedom.R
import com.bellogate.voiceoffreedom.ui.MainActivity

@Keep
class SyncNotificationManager {

    companion object{

        private val CHANNEL_ID = "com.seamfix.bioregistra"
        private val id = 1
        private lateinit var notificationBuilder: NotificationCompat.Builder
        private lateinit var notificationManager : NotificationManager

        fun create(context: Context){
            createNotificationChannel(context)

            notificationBuilder =
                NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)//change to BR LOGO
                    .setContentTitle("Voice of Freedom")
                    .setContentText("Uploading devotionals...")
                    .setAutoCancel(true)

            notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }


        fun showNotificationForAppUpdate(context: Context, numberOfDevotionalsRemaining: Int, totalDevotionalsToSync: Int){

            val intent: Intent =
                Intent(context, MainActivity::class.java).
                    putExtra("numberOfDevotionalsRemaining", numberOfDevotionalsRemaining)
                    .putExtra("totalDevotionalsToSync", totalDevotionalsToSync)

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT
            )

            if(numberOfDevotionalsRemaining == 0){
                notificationBuilder.setContentText("Upload complete!")
            }

            notificationBuilder.setContentIntent(pendingIntent)
            //since the value of 'numberOfDevotionalsRemaining' is actually going to be reducing at every iteration,
            //we need to: (totalDevotionalsToSync - numberOfDevotionalsRemaining) as a trick to have a high value
            //to compare with the 'totalDevotionalsToSync', if not, our notification bar will be reducing instead of
            //increasing.
            notificationBuilder.setProgress(totalDevotionalsToSync,
                (totalDevotionalsToSync - numberOfDevotionalsRemaining),
                false)
            notificationManager.notify(id, notificationBuilder.build())
        }


        private fun createNotificationChannel(context: Context) { //If you don't call this method, you notifications will only show on older versions of android phones.
            val CHANNEL_NAME = "voice_of_freedom"
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_LOW// IMPORTANCE_LOW means this
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

}