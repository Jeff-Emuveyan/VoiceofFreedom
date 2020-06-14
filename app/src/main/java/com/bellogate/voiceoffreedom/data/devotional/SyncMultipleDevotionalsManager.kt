package com.bellogate.voiceoffreedom.data.devotional

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bellogate.voiceoffreedom.model.Devotional
import com.bellogate.voiceoffreedom.ui.devotional.add.DevotionalCollectorItem
import com.bellogate.voiceoffreedom.util.DEVOTIONALS
import com.bellogate.voiceoffreedom.util.logCollectors
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream


class SyncMultipleDevotionalsManager(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {

    companion object{

        lateinit var context: Context
        var numberOfCollectors: Int = 1 //default
        var listOfCollectors = LinkedHashMap<String, DevotionalCollectorItem>()
        lateinit var  uploadTask: StorageTask<UploadTask.TaskSnapshot>


        fun validateInput(context: Context, validateInput: (Boolean)-> Unit){
            this.context = context

            AlertDialog.Builder(context)
                .setTitle("Upload")
                .setMessage("Upload these devotionals?")
                .setPositiveButton("Yes") { _, _ ->

                    if(validateCollectors()){
                        validateInput.invoke(true)
                    }else{
                        validateInput.invoke(false)
                    }
                }
                .setNegativeButton("No") { _, _ -> }
                .show()
        }


        private fun validateCollectors(): Boolean{

            logCollectors()

            if(listOfCollectors.isNotEmpty()) {
                for (map in listOfCollectors.entries) {
                    val devotionalCollectorItem = map.value
                    if (devotionalCollectorItem.dateInMillis == null ||
                            devotionalCollectorItem.dateInSimpleForm == null ||
                            devotionalCollectorItem.imageUri == null){
                        return false
                    }
                }
                return true
            }
            return false
        }



        private fun sync(context: Context){

            if(listOfCollectors.isNotEmpty()) {

                val initialListSize = listOfCollectors.size

                //get the first devotionalCollectorItem on the list:
                val devotionalCollectorItem = listOfCollectors.entries.first().value

                //Gotten from: https://github.com/firebase/snippets-android/blob/e16846813135fde4fd6e8823948cfae61e17fd57/storage/app/src/main/java/com/google/firebase/referencecode/storage/kotlin/StorageActivity.kt#L408-L409
                val bitmap = (devotionalCollectorItem.imageView.drawable as BitmapDrawable).bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()

                val storage = Firebase.storage
                val reference = storage.reference

                val imageRef: StorageReference = reference.child("${DEVOTIONALS}/${devotionalCollectorItem.dateInMillis}.jpg")

                uploadTask = imageRef.putBytes(data).addOnCompleteListener {
                    if(it.isSuccessful){
                        //Now, sync the devotional object for this image:
                        imageRef.downloadUrl.addOnSuccessListener {uri ->
                            val devotional = Devotional(devotionalCollectorItem.dateInMillis.toString(),
                                devotionalCollectorItem.dateInSimpleForm!!,
                                uri.toString())

                            DevotionalRepository(context).syncDevotional(devotional){ success, message ->
                                if(success){
                                    //delete the devotionalCollectorItem from the list:
                                    listOfCollectors.remove(devotionalCollectorItem.id)

                                    //show notification:
                                    SyncNotificationManager.showNotificationForAppUpdate(context, listOfCollectors.size, initialListSize)
                                    Log.e(SyncMultipleDevotionalsManager::class.java.simpleName
                                        , "Uploaded: list size :${listOfCollectors.size}")

                                    //restart the process until the list is empty:
                                    sync(context)
                                }else{
                                    Toast.makeText(context, "Failed to upload: $message", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }else{
                        Toast.makeText(context, "Upload has stopped", Toast.LENGTH_LONG).show()

                    }
                }

                uploadTask.addOnCanceledListener {
                    //if the user cancels, we clear the list, hence ending the upload process
                    listOfCollectors.clear()
                    //this will now remove the notification for the screen:
                    SyncNotificationManager.cancelNotification()
                    Toast.makeText(context, "Upload cancelled", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun doWork(): Result {
        SyncNotificationManager.create(context)
        sync(context)
        return Result.success()
    }

}