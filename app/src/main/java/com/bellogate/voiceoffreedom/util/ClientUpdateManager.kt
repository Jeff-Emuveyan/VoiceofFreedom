package com.bellogate.voiceoffreedom.util
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

@Keep
class ClientUpdateManager {

    companion object{
        private lateinit var activity: AppCompatActivity
        lateinit var appUpdateManager: AppUpdateManager
        const val UPDATE_REQUEST_CODE = 33


        /** Call this method first to initialize **/
        fun init(activity: AppCompatActivity){
            this.activity = activity
            appUpdateManager  = AppUpdateManagerFactory.create(activity)
        }


        /*** The checks to know if there is an update ready ***/
        fun requireUpdate(){
            // Returns an intent object that you use to check for an update.
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo

            // Checks that the platform will allow the specified type of update.
            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    // For a flexible update, use AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                ) {
                    // Request the update.
                    appUpdateManager.startUpdateFlowForResult(
                        // Pass the intent that is returned by 'getAppUpdateInfo()'.
                        appUpdateInfo,
                        // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                        AppUpdateType.IMMEDIATE,
                        // The current activity making the update request.
                        activity,
                        // Include a request code to later monitor this update request.
                        UPDATE_REQUEST_CODE
                    )
                }
            }
        }


        /**
         * Checks that the update is not stalled during 'onResume()'.
           However, you should execute this check at all entry points into the app.
         * **/
        fun listenForUpdateStateWhenResume(activity: AppCompatActivity, appUpdateManager: AppUpdateManager){
            appUpdateManager
                .appUpdateInfo
                .addOnSuccessListener { appUpdateInfo ->
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                        // If an in-app update is already running, resume the update.
                        appUpdateManager.startUpdateFlowForResult(appUpdateInfo,
                            AppUpdateType.IMMEDIATE, activity, UPDATE_REQUEST_CODE)
                    }
                }

        }
    }

}