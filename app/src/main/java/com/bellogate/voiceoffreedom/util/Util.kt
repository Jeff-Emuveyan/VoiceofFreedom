package com.bellogate.voiceoffreedom.util

import android.app.DatePickerDialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.bellogate.voiceoffreedom.BuildConfig
import com.bellogate.voiceoffreedom.R
import com.bellogate.voiceoffreedom.data.UserRepository
import com.bellogate.voiceoffreedom.ui.devotional.add.DevotionalCollectorItem
import com.bellogate.voiceoffreedom.data.devotional.SyncMultipleDevotionalsManager
import com.bellogate.voiceoffreedom.model.Admin
import com.bellogate.voiceoffreedom.model.User
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.greentoad.turtlebody.mediapicker.MediaPicker
import com.greentoad.turtlebody.mediapicker.core.MediaPickerConfig
import kotlinx.coroutines.CoroutineScope
import java.io.ByteArrayOutputStream
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*


const val AMOUNT = "amount"
const val STOP_NOTIFICATION = "stop_notification"
const val Progress = "progress"
const val TotalFileSize = "total"

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
    .setUriPermanentAccess(false)
    .setAllowMultiSelection(false)
    .setShowConfirmationDialog(true)
    .setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)


fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? =
    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)


fun selectImage(activity: FragmentActivity, result: (uri:Uri, filePath: String) -> Unit){
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
            result.invoke(uri, "filePath")
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