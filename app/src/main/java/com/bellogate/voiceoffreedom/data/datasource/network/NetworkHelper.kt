package com.bellogate.voiceoffreedom.data.datasource.network

import com.bellogate.voiceoffreedom.util.ADMIN
import com.bellogate.voiceoffreedom.model.Admin
import com.bellogate.voiceoffreedom.model.Key
import com.bellogate.voiceoffreedom.model.User
import com.bellogate.voiceoffreedom.util.KEY
import com.bellogate.voiceoffreedom.util.USER
import com.google.firebase.firestore.FirebaseFirestore

class NetworkHelper {

    companion object{

        private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

        /** Gets all Admin from the database***/
        fun fetchAllAdmin(fetched:(success: Boolean, result: ArrayList<Admin>?) -> Unit) =
            db.collection(ADMIN).get()
                .addOnCompleteListener {
                    val listOfAdmin = arrayListOf<Admin>()
                    if(it.isSuccessful && it.result != null){
                        for(document in it.result!!){
                            val admin = document.toObject(Admin::class.java)
                            listOfAdmin.add(admin)
                        }
                        fetched.invoke(true, listOfAdmin)
                    }else{
                        fetched.invoke(false, null)
                    }
                }
                .addOnFailureListener {
                    fetched.invoke(false, null)
                }



        /** Syncs a User to Firebase */
        fun syncUser(user: User, saved: (saved: Boolean) -> Unit){
            db.collection(USER).document(user.timeCreated.toString()).set(user).addOnCompleteListener {
                if(it.isSuccessful){
                    saved.invoke(true)
                }else{
                    saved.invoke(false)
                }
            }
        }


        /*** Get user from Firestore ***/
        fun getUser(userEmail: String, user: (User?) -> Unit){
            db.collection(USER).whereEqualTo("email", userEmail).limit(1)
                .get().addOnSuccessListener {
                    for(document in it.documents){//this will only have one document because we set the limit to 1
                        val userFromFirestore = document.toObject(User::class.java)
                        user.invoke(userFromFirestore)
                    }
                }.addOnFailureListener {
                    user.invoke(null)
                }
        }


        /** Fetch the Public secret key to be used **/
        fun getKey(response:(success: Boolean, key: Key?)-> Unit){
            db.collection(KEY).limit(1).get().addOnSuccessListener {

                if(it.documents.isNullOrEmpty() || it.documents.size == 0){
                    response.invoke(false, null)
                    return@addOnSuccessListener
                }

                for(document in it.documents){//this will only have one document because we set the limit to 1
                    val key = document.toObject(Key::class.java)
                    response.invoke(true, key)
                }
                if(it.documents.isNullOrEmpty() || it.documents.size == 0){
                    response.invoke(false, null)
                }
            }.addOnFailureListener {
                response.invoke(false, null)
            }
        }
    }
}