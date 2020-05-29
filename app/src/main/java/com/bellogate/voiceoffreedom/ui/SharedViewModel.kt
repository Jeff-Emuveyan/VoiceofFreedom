package com.bellogate.voiceoffreedom.ui

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bellogate.voiceoffreedom.data.BaseRepository
import com.bellogate.voiceoffreedom.data.setup.UserRepository
import com.bellogate.voiceoffreedom.model.User
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope


/**
 * Serves as a SharedViewModel and a Base class for all ViewModels
 * */
open class SharedViewModel: ViewModel() {

    /** Choose authentication providers **/
    fun getAuthProviders() = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build())


    /****
     *Returns a LiveData User object that all Fragments can observe
     */
    fun getUser(context: Context, id: Int) = UserRepository(context).getUser(id)


    fun saveUser(context: Context, coroutineScope: CoroutineScope, id: Int, newUser: User) =
        UserRepository(context).saveUser(coroutineScope, id, newUser)


    fun logout(context: Context) = UserRepository(context).logout()


    val startSignInProcess = MutableLiveData<Boolean>()

    /**
     * Used to know whether the user has signed out
     **/
    fun listenForUserSignOut(context: Context) = BaseRepository(context).listenForUserSignOut {
        if (!it){//user has Signed Out
            //delete user from db:
            UserRepository(context).deleteUser(viewModelScope, 1)
        }
    }



    fun handleSuccessfulSignIn(context: Context,
                               idpResponse: IdpResponse, saveSuccessful: (Boolean) -> Unit){

        if(idpResponse.isNewUser){
            // Successfully signed up
            val fireBaseUser = FirebaseAuth.getInstance().currentUser
            val user = User(1, fireBaseUser?.displayName ?: "You", fireBaseUser!!.email!!,
                System.currentTimeMillis(), false)

            //finally save the new user:
            saveUser(context, viewModelScope, 1, user)
            saveSuccessful.invoke(true)

        }else{//this was a sign in operation because the user already existed, so:

            //fetch the user's details from Firestore
            UserRepository(context).getUserFromNetwork(idpResponse.email!!){

                if(it != null){//save the user to local db:
                    //Note: this 'saveUser' will first write the user to Firestore before saving it to local db.
                    //You may be concerned that this will create a duplicate user in Firestore. It will not.
                    //Firestore will simply replace the old document with this new one since they both
                    //have the same ID.
                    saveUser(context, viewModelScope, 1, it)
                    saveSuccessful.invoke(true)
                }else{
                    saveSuccessful.invoke(false)
                }
            }
        }

    }
}