package com.bellogate.voiceoffreedom.data.devotional

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.widget.Toast
import com.bellogate.voiceoffreedom.model.Devotional
import com.bellogate.voiceoffreedom.ui.devotional.add.DevotionalCollectorItem
import com.bellogate.voiceoffreedom.util.DEVOTIONALS
import com.bellogate.voiceoffreedom.util.logCollectors
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream


class SyncMultipleDevotionalsManager {

    companion object{

        var numberOfCollectors: Int = 1 //default

        var listOfCollectors = LinkedHashMap<String, DevotionalCollectorItem>()

        fun syncDevotionals(context: Context, onStart:()-> Unit, invalideInput: (String)-> Unit){
            AlertDialog.Builder(context)
                .setTitle("Upload")
                .setMessage("Upload these devotionals?")
                .setPositiveButton("Yes") { _, _ ->

                    if(validateCollectors()){
                        onStart.invoke()
                        doWork(context, listOfCollectors)
                    }else{
                        invalideInput.invoke("Missing image or date")
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
                    return !(devotionalCollectorItem.dateInMillis == null ||
                            devotionalCollectorItem.dateInSimpleForm == null ||
                            devotionalCollectorItem.imageUri == null)
                }
            }
            return true
        }



        private fun doWork(context: Context, list: LinkedHashMap<String, DevotionalCollectorItem>){

            if(list.isNotEmpty()) {
                for (map in listOfCollectors.entries) {
                    val devotionalCollectorItem = map.value

                    //Gotten from: https://github.com/firebase/snippets-android/blob/e16846813135fde4fd6e8823948cfae61e17fd57/storage/app/src/main/java/com/google/firebase/referencecode/storage/kotlin/StorageActivity.kt#L408-L409
                    val bitmap = (devotionalCollectorItem.imageView.drawable as BitmapDrawable).bitmap
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()

                    val storage = Firebase.storage
                    val reference = storage.reference

                    val imageRef: StorageReference = reference.child("${DEVOTIONALS}/${devotionalCollectorItem.dateInMillis}.jpg")

                    val uploadTask = imageRef.putBytes(data).addOnCompleteListener {
                        if(it.isSuccessful){
                            //Now, sync the devotional object for this image:
                            imageRef.downloadUrl.addOnSuccessListener {uri ->
                                val devotional = Devotional(devotionalCollectorItem.dateInMillis.toString(),
                                    devotionalCollectorItem.dateInSimpleForm!!,
                                    uri.toString())

                                DevotionalRepository(context).syncDevotional(devotional){ success, message ->
                                    if(success){
                                        Toast.makeText(context, "Uploaded", Toast.LENGTH_LONG).show()
                                    }else{
                                        Toast.makeText(context, "Failed to upload: $message", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }else{
                            Toast.makeText(context, "Failed to upload", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

}