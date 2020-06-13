package com.bellogate.voiceoffreedom.data.datasource.network

import com.bellogate.voiceoffreedom.util.ADMINS
import com.bellogate.voiceoffreedom.model.Admin
import com.bellogate.voiceoffreedom.model.Devotional
import com.bellogate.voiceoffreedom.model.Key
import com.bellogate.voiceoffreedom.model.User
import com.bellogate.voiceoffreedom.ui.devotional.util.UIState
import com.bellogate.voiceoffreedom.util.DEVOTIONALS
import com.bellogate.voiceoffreedom.util.KEY
import com.bellogate.voiceoffreedom.util.USERS
import com.google.firebase.firestore.FirebaseFirestore

class NetworkHelper {

    companion object {

        private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

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


        fun getDevotionalByDate(dateToFind: String, response: (UIState, Devotional?) -> Unit) {
            db.collection(DEVOTIONALS).whereEqualTo("dateInSimpleFormat", dateToFind)
                .limit(1).get().addOnSuccessListener {

                    if (it.documents.isNullOrEmpty() || it.documents.size == 0) {
                        //search was successful but no devotional matched that 'timeInMilliSeconds'
                        response.invoke(UIState.NO_DATA_FOR_SELECTED_DATE, null)
                    } else {
                        for (document in it.documents) {//this will only have one document because we set the limit to 1
                            val devotional = document.toObject(Devotional::class.java)
                            if (devotional != null) {
                                response.invoke(UIState.FOUND, devotional)
                            }
                        }
                    }

                }.addOnFailureListener {
                    response.invoke(UIState.FAILED_TO_LOAD, null)
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


        fun syncDevotional(it: Devotional, success:(Boolean, String?)-> Unit) {
            db.collection(DEVOTIONALS).document(it.dateInMilliSeconds).set(it)
                .addOnSuccessListener {
                    success.invoke(true, null)
                }
                .addOnFailureListener { e ->
                    success.invoke(false, e.message)
                }
        }
    }
}