package com.bellogate.voiceoffreedom.data.datasource.network

import android.net.Uri
import com.bellogate.voiceoffreedom.model.*
import com.bellogate.voiceoffreedom.ui.devotional.util.DevotionalUIState
import com.bellogate.voiceoffreedom.ui.media.video.VideoUIState
import com.bellogate.voiceoffreedom.util.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import java.io.File

class NetworkHelper {

    companion object {

        private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
        var  videoUploadTask: StorageTask<UploadTask.TaskSnapshot>? = null

        /** Gets all Admin from the database***/
        fun fetchAllAdmin(fetched: (success: Boolean, result: ArrayList<Admin>?) -> Unit) =
            db.collection(ADMINS).get()
                .addOnCompleteListener {
                    val listOfAdmin = arrayListOf<Admin>()
                    if (it.isSuccessful && it.result != null) {
                        for (document in it.result!!) {
                            val admin = document.toObject(Admin::class.java)
                            listOfAdmin.add(admin)
                        }
                        fetched.invoke(true, listOfAdmin)
                    } else {
                        fetched.invoke(false, null)
                    }
                }
                .addOnFailureListener {
                    fetched.invoke(false, null)
                }


        /** Syncs a User to Firebase */
        fun syncUser(user: User, saved: (saved: Boolean) -> Unit) {
            db.collection(USERS).document(user.timeCreated.toString()).set(user)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        saved.invoke(true)
                    } else {
                        saved.invoke(false)
                    }
                }
        }


        /*** Get user from Firestore ***/
        fun getUser(userEmail: String, user: (User?) -> Unit) {
            db.collection(USERS).whereEqualTo("email", userEmail).limit(1)
                .get().addOnSuccessListener {
                    for (document in it.documents) {//this will only have one document because we set the limit to 1
                        val userFromFirestore = document.toObject(User::class.java)
                        user.invoke(userFromFirestore)
                    }
                }.addOnFailureListener {
                    user.invoke(null)
                }
        }


        /** Fetch the Public secret key to be used **/
        fun getKey(response: (success: Boolean, key: Key?) -> Unit) {
            db.collection(KEY).limit(1).get().addOnSuccessListener {

                if (it.documents.isNullOrEmpty() || it.documents.size == 0) {
                    response.invoke(false, null)
                } else {
                    for (document in it.documents) {//this will only have one document because we set the limit to 1
                        val key = document.toObject(Key::class.java)
                        response.invoke(true, key)
                    }
                }
            }.addOnFailureListener {
                response.invoke(false, null)
            }
        }


        fun getDevotionalByDate(dateToFind: String, response: (DevotionalUIState, Devotional?) -> Unit) {
            db.collection(DEVOTIONALS).whereEqualTo("dateInSimpleFormat", dateToFind)
                .limit(1).get().addOnSuccessListener {

                    if (it.documents.isNullOrEmpty() || it.documents.size == 0) {
                        //search was successful but no devotional matched that 'timeInMilliSeconds'
                        response.invoke(DevotionalUIState.NO_DATA_FOR_SELECTED_DATE, null)
                    } else {
                        for (document in it.documents) {//this will only have one document because we set the limit to 1
                            val devotional = document.toObject(Devotional::class.java)
                            if (devotional != null) {
                                response.invoke(DevotionalUIState.FOUND, devotional)
                            }
                        }
                    }

                }.addOnFailureListener {
                    response.invoke(DevotionalUIState.FAILED_TO_LOAD, null)
                }
        }


        fun deleteDevotional(it: Devotional, success:(Boolean, String?)-> Unit){
            db.collection(DEVOTIONALS).document(it.dateInMilliSeconds)
                .delete()
                .addOnSuccessListener {
                    success.invoke(true, null)
                }
                .addOnFailureListener { e ->
                    success.invoke(false, e.message)
                }
        }



        /*** Used to delete an image file in Firebase Storage **/
        fun deleteDevotionalImageFile(timeInMilliSeconds: String, success:(Boolean, String?)-> Unit){
            val storage = Firebase.storage
            val reference = storage.reference

            val imageRef: StorageReference = reference.child("${DEVOTIONALS}/${timeInMilliSeconds}.jpg")
            imageRef.delete()
                .addOnSuccessListener {
                    success.invoke(true, null)
                }.addOnFailureListener { e ->
                    success.invoke(false, e.message)
                }
        }


        fun syncDevotional(it: Devotional, success:(Boolean, String?)-> Unit) {
            db.collection(DEVOTIONALS).document(it.dateInMilliSeconds).set(it)
                .addOnSuccessListener {
                    success.invoke(true, null)
                }
                .addOnFailureListener { e ->
                    success.invoke(false, e.message)
                }
        }


        fun syncVideo(it: Video, success:(Boolean, String?)-> Unit) {
            db.collection(VIDEOS).document(it.dateInMilliSeconds!!).set(it)
                .addOnSuccessListener {
                    success.invoke(true, null)
                }
                .addOnFailureListener { e ->
                    success.invoke(false, e.message)
                }
        }


        fun fetchVideos(response:(VideoUIState, ArrayList<Video?>?)-> Unit){
            db.collection(VIDEOS).orderBy(DATE_IN_MILLISECONDS).limit(6).get()
                .addOnSuccessListener {

                    if (it.documents.isNullOrEmpty() || it.documents.size == 0) {
                        //search was successful but no devotional matched that 'timeInMilliSeconds'
                        response.invoke(VideoUIState.NO_VIDEOS, null)
                    } else {
                        val list: ArrayList<Video?>? = ArrayList<Video?>()
                        for (document in it.documents) {//this will only have one document because we set the limit to 1
                            val video = document.toObject(Video::class.java)
                            if (video != null) {
                                list?.add(video)
                            }
                        }
                        response.invoke(VideoUIState.FOUND, list)
                    }

                }.addOnFailureListener {
                    response.invoke(VideoUIState.ERROR, null)
                }
        }


    }

}