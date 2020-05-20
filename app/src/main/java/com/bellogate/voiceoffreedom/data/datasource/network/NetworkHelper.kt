package com.bellogate.voiceoffreedom.data.datasource.network

import com.bellogate.voiceoffreedom.util.ADMIN
import com.bellogate.voiceoffreedom.model.Admin
import com.bellogate.voiceoffreedom.model.User
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



    }
}