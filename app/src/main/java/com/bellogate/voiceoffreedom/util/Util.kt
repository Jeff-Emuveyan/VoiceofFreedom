package com.bellogate.voiceoffreedom.util

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import com.bellogate.voiceoffreedom.BuildConfig
import com.bellogate.voiceoffreedom.R
import com.bellogate.voiceoffreedom.data.UserRepository
import com.bellogate.voiceoffreedom.data.devotional.SyncMultipleDevotionalsManager
import com.bellogate.voiceoffreedom.model.Admin
import com.bellogate.voiceoffreedom.model.User
import com.bellogate.voiceoffreedom.ui.MainActivity
import com.bellogate.voiceoffreedom.ui.devotional.add.DevotionalCollectorItem
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.greentoad.turtlebody.mediapicker.MediaPicker
import com.greentoad.turtlebody.mediapicker.core.MediaPickerConfig
import kotlinx.coroutines.CoroutineScope
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


const val AMOUNT = "amount"
const val STOP_NOTIFICATION = "stop_notification"
const val Progress = "progress"
const val TotalFileSize = "total"
const val UUID = "uuid"
const val NO_VALUE_SET = -1L

//WorkManager tags
const val SYNC_VIDEO = "sync_video"

fun showSnackMessage(view: View, message: String){
    Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
}

fun showSnackMessageAtTop(context: Context, view: View, message: String){
    val snackBarView = Snackbar.make(view, message , Snackbar.LENGTH_LONG)
    snackBarView.duration = 10000
    val view = snackBarView.view
    val params = view.layoutParams as FrameLayout.LayoutParams
    params.gravity = Gravity.TOP
    view.layoutParams = params
    view.background = ContextCompat.getDrawable(context, R.color.green) // for custom background
    snackBarView.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
    snackBarView.show()
}


fun isStagingBuild(): Boolean {
    return BuildConfig.FLAVOR == "staging"
}

fun isProductionBuild(): Boolean {
    return BuildConfig.FLAVOR == "production"
}



fun Fragment.showAlertForSuccessfulPayment(){

    val layout = LayoutInflater.from(this.requireContext())

    val view: View = layout.inflate(R.layout.alert_payment_successful_layout, null)

    val alertBuilder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())

    alertBuilder.setView(view)
    alertBuilder.setCancelable(false)
    val alert = alertBuilder.show()

    val okButton = view.findViewById<Button>(R.id.okButton)
    okButton.setOnClickListener{
        alertBuilder.setCancelable(true)
        alert.dismiss()
        findNavController().popBackStack()
        findNavController().popBackStack()
    }

}


fun Fragment.showAlert(title: String, message: String){
    val alertBuilder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
    alertBuilder.setTitle(title)
    alertBuilder.setMessage(message)
    alertBuilder.setPositiveButton("Close"
    ) { _, _ -> }
    alertBuilder.show()
}


fun showAlert(context: Context, title: String, message: String, action: (Boolean) -> Unit){
    val alertBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
    alertBuilder.setTitle(title)
    alertBuilder.setMessage(message)
    alertBuilder.setPositiveButton("OK"
    ) { _, _ -> action.invoke(true)}
    alertBuilder.show()
}



fun getSimpleDateFormat(timestampValue: Long,simpleDateFormat: String?): String {
    val dateValue = Date(timestampValue)
    val dateFormat = SimpleDateFormat(simpleDateFormat, Locale.ENGLISH)
    val symbols = DateFormatSymbols(Locale.getDefault())
    dateFormat.dateFormatSymbols = symbols
    return dateFormat.format(dateValue)
}


fun todayDate(dateInMilliSeconds: Long) = getSimpleDateFormat(dateInMilliSeconds, "dd-MMM-yyyy")

fun showDatePickerDialog(context: Context, shouldHaveMaxDate: Boolean, listener: DatePickerDialog.OnDateSetListener) {
    val calendar = Calendar.getInstance()
    val year = calendar[Calendar.YEAR]
    val month = calendar[Calendar.MONTH]
    val day = calendar[Calendar.DAY_OF_MONTH]
    val datePickerTag = "Select date"
    val maximumDate = Calendar.getInstance()
    maximumDate[Calendar.YEAR] = year
    maximumDate[Calendar.MONTH] = month
    maximumDate[Calendar.DAY_OF_MONTH] = day
    val datePickerDialog = DatePickerDialog(context, listener, year, month, day)
    if(shouldHaveMaxDate) {
        datePickerDialog.datePicker.maxDate = maximumDate.timeInMillis
    }
    datePickerDialog.datePicker.tag = datePickerTag
    datePickerDialog.setTitle(datePickerTag)
    datePickerDialog.show()
}


val pickerConfig = MediaPickerConfig()
    .setUriPermanentAccess(true)
    .setAllowMultiSelection(false)
    .setShowConfirmationDialog(true)
    .setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)


fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? =
    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)


fun selectImage(activity: FragmentActivity, result: (uri:Uri, file: File) -> Unit){
    val pickerConfig = pickerConfig

    val subscribe = MediaPicker.with(activity!!, MediaPicker.MediaTypes.IMAGE)
        .setConfig(pickerConfig)
        .setFileMissingListener(object : MediaPicker.MediaPickerImpl.OnMediaListener {
            override fun onMissingFileWarning() {
                Toast.makeText(activity, "Missing file", Toast.LENGTH_LONG).show()
            }
        })
        .onResult()
        .subscribe({
            val uri = it[0]
            val file = FileUtil.from(activity, uri)
            result.invoke(uri, file)
        }, {
            it.printStackTrace()
        })
}

fun selectAudio(activity: FragmentActivity, result: (uri:Uri, file: File) -> Unit){
    val pickerConfig = pickerConfig

    val subscribe = MediaPicker.with(activity!!, MediaPicker.MediaTypes.AUDIO)
        .setConfig(pickerConfig)
        .setFileMissingListener(object : MediaPicker.MediaPickerImpl.OnMediaListener {
            override fun onMissingFileWarning() {
                Toast.makeText(activity, "Missing file", Toast.LENGTH_LONG).show()
            }
        })
        .onResult()
        .subscribe({
            val uri = it[0]
            val file = FileUtil.from(activity, uri)
            result.invoke(uri, file)
        }, {
            it.printStackTrace()
        })
}


fun Fragment.centerToast(message: String){
    val toast = Toast.makeText(requireContext(), message, Toast.LENGTH_LONG)
    toast.setGravity(Gravity.CENTER, 0, 0)
    toast.show()
}


fun updateCollectedItems(devotionalCollectorItem: DevotionalCollectorItem){

    if(SyncMultipleDevotionalsManager.listOfCollectors.containsKey(devotionalCollectorItem.id)){
        //remove the old collector from the list:
        SyncMultipleDevotionalsManager.listOfCollectors.remove(devotionalCollectorItem.id)
        //add the updated collector to the list again
        SyncMultipleDevotionalsManager.listOfCollectors[devotionalCollectorItem.id!!] =
            devotionalCollectorItem
    }else{
        //add the updated collector to the list
        SyncMultipleDevotionalsManager.listOfCollectors[devotionalCollectorItem.id!!] =
            devotionalCollectorItem
    }
}

fun deleteCollectedItem(devotionalCollectorItem: DevotionalCollectorItem){

    if(SyncMultipleDevotionalsManager.listOfCollectors.containsKey(devotionalCollectorItem.id)){

        SyncMultipleDevotionalsManager.listOfCollectors.remove(devotionalCollectorItem.id)
        logCollectors()
    }
}


fun logCollectors(){
    if(SyncMultipleDevotionalsManager.listOfCollectors.isNotEmpty()) {
        Log.e(
            SyncMultipleDevotionalsManager::class.java.simpleName,
            "Total size is: ${SyncMultipleDevotionalsManager.listOfCollectors.size}"
        )
        for (map in SyncMultipleDevotionalsManager.listOfCollectors.entries) {
            Log.e(
                SyncMultipleDevotionalsManager::class.java.simpleName,
                "key: ${map.key} value: ${map.value.toString()}"
            )
        }
    }
}


fun Fragment.alertWithAction(title: String, message: String, confirmed: (Boolean)-> Unit){
    AlertDialog.Builder(requireContext()).setTitle(title).setMessage(message).setPositiveButton("Yes"
    ) { _, _ ->
        confirmed.invoke(true)
    }.setNegativeButton("No"
    ) { _, _ ->
        confirmed.invoke(false)
    }.show()
}


/**
 * This will update the user's admin status in Firebase and Room DB
 */
fun updateUserAdminStatus(context: Context, user: User, adminList: ArrayList<Admin>?,
                                  viewModelScope: CoroutineScope
){
    val repository =
        UserRepository(context)

    adminList?.let {
        for(admin in it){
            if(admin.email == user.email){//update the user to an admin
                user.isAdmin = true
                repository.updateUser(viewModelScope, user)
                return
            }else{//this means that the present user should not be an admin
                user.isAdmin = false
                repository.updateUser(viewModelScope, user)
            }
        }
    }
}


fun Bitmap.toBytes(): ByteArray{
    val baos = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    return baos.toByteArray()
}



/*** Get the percentage of a value when compared to another value **/
fun getPercentFromValues(valueToBeComparedWith: Long, valueProvided: Long): Long{

    return (valueProvided * 100) / valueToBeComparedWith
}


fun showNotification(context: Context, title: String, body: String) {
    val intent: Intent = Intent(context, MainActivity::class.java)

    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    val pendingIntent = PendingIntent.getActivity(
        context, 0 /* Request code */, intent,
        PendingIntent.FLAG_ONE_SHOT
    )
    val CHANNEL_ID = "com.bellogate.caliphate"
    val defaultSoundUri = RingtoneManager.getDefaultUri(
        RingtoneManager.TYPE_NOTIFICATION
    )
    val notificationBuilder =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.app_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body)
            )
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(Random().nextInt(), notificationBuilder.build())
}


fun getDuration(context: Context, mediaUri: Uri): String {

    val mp: MediaPlayer = MediaPlayer.create(context, mediaUri)
    val duration = mp.duration
    mp.release()

    return  String.format("%d min, %d sec",
        TimeUnit.MILLISECONDS.toMinutes(duration.toLong()),
        TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration.toLong())))
}


fun createForegroundInfo(context: Context, uuid: UUID, max: Long, progress: Long): ForegroundInfo { // Build a notification using bytesRead and contentLength
    val id = "com.bellogate.caliphate"
    val title = "Uploading..."
    val cancel = "Stop"
    // This PendingIntent can be used to cancel the worker
    val intent = WorkManager.getInstance(context)
        .createCancelPendingIntent(uuid)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        createNotificationChannel(context)
    }
    val notification: Notification = NotificationCompat.Builder(context, id)
        .setContentTitle(title)
        .setTicker(title)
        .setSmallIcon(android.R.drawable.alert_light_frame)
        .setProgress(max.toInt(), progress.toInt(), false)
        .setOngoing(true) // Add the cancel action to the notification which can
        // be used to cancel the worker
        .addAction(android.R.drawable.ic_delete, cancel, intent)
        .build()
    return ForegroundInfo(33, notification)
}


private fun createNotificationChannel(context: Context) { //If you don't call this method, you notifications will only show on older versions of android phones.
    val CHANNEL_NAME = "voice_of_freedom"
    val CHANNEL_ID = "com.bellogate.caliphate"
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


fun showMediaPopUpMenu(context: Context, user: User?, view: View, onMenuClicked: (MenuItem)-> Unit){

    val popupMenu = PopupMenu(context, view)
    popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

    //control menu items based on the user's privilege:
    if(user == null || !user.isAdmin){//user should not see the delete menu:
        popupMenu.menu.getItem(1).isVisible = false
    }
    popupMenu.setOnMenuItemClickListener {
        onMenuClicked.invoke(it)
        true
    }

    popupMenu.show()

}

/**** Used to download a file (audio or video) ***/
fun downloadFile(context: Context, url: String){
    showAlert(context, "Download file?", "Do you want to download this file?"){
        val downloadManager =  context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri);
        request.setTitle("Voice of Freedom");
        request.setDescription("Downloading file...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, uri.lastPathSegment);
        downloadManager.enqueue(request)
    }
}
