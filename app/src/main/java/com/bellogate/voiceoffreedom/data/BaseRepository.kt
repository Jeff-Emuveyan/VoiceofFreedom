package com.bellogate.voiceoffreedom.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.bellogate.voiceoffreedom.data.datasource.database.AppDatabase
import com.bellogate.voiceoffreedom.data.datasource.network.NetworkHelper
import com.bellogate.voiceoffreedom.model.User
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

open class BaseRepository(val context: Context) {

    val db = AppDatabase.getDatabase(context)


    fun logout(){
        AuthUI.getInstance().signOut(context).addOnCompleteListener {
            //this call will cause listenForUserSignOut in MainActivity to trigger.
            if (it.isSuccessful){
                Log.e("User state", "Signed Out")
            }
        }.addOnFailureListener {
            //this call will cause listenForUserSignOut in MainActivity to trigger.
            Log.e("User state", "Signed Out failed")
        }
    }


    /**
     * A listener to notify us if the user has signed out of Firebase*
     * ***/
    fun listenForUserSignOut(userIsLoggedIn: (Boolean)->Unit) =
        FirebaseAuth.getInstance().addAuthStateListener {
            if(it.currentUser == null){
                userIsLoggedIn.invoke(false)
            }else{
                userIsLoggedIn.invoke(true)
            }

        }

}