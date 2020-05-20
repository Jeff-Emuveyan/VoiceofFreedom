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

    /***fetch a user asynchronously ie returns a Livedata so there is no need to make it suspend.
     * It is already asynchronous**/
    fun getUser(id: Int): LiveData<User?> = db.userDao().getUser(id)

    /***fetch a user synchronously ie does not return Livedata. Room won't let us read data synchronously,
     *So we must either return a LiveData which will do the async work or make this function suspend**/
    suspend fun getUserSynchronously(id: Int) = db.userDao().getUserSynchronously(id)


    /** Updates User to Firebase and later to Room database **/
    fun updateUser(coroutineScope: CoroutineScope, user: User){
        NetworkHelper.syncUser(user) {
            if (it) {
                coroutineScope.launch {
                    db.userDao().updateUser(user)
                }
            }
        }
    }


    /** Saves user to Firebase and later to Room database **/
    fun saveUser(coroutineScope: CoroutineScope, id: Int, newUser: User){
        NetworkHelper.syncUser(newUser){
            if (it){
                coroutineScope.launch {
                    val oldUser = getUserSynchronously(id)
                    if (oldUser == null){
                        db.userDao().saveUser(newUser)
                    }else{
                        db.userDao().updateUser(newUser)
                    }
                }
            }
        }
    }


    /***delete**/
    fun deleteUser(coroutineScope: CoroutineScope, id: Int){
        coroutineScope.launch {
            val user = getUserSynchronously(id)
            if (user != null){
                db.userDao().deleteUser(user)
            }
        }
    }



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