package com.bellogate.voiceoffreedom.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bellogate.voiceoffreedom.data.BaseRepository
import com.bellogate.voiceoffreedom.data.UserRepository
import com.bellogate.voiceoffreedom.data.setup.AdminRepository
import com.bellogate.voiceoffreedom.model.User
import com.bellogate.voiceoffreedom.ui.setup.SetupActivityViewModel
import com.bellogate.voiceoffreedom.util.Fragments
import com.bellogate.voiceoffreedom.util.updateUserAdminStatus
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope

const val PERMISSION_ID = 200
/**
 * Serves as a SharedViewModel for communication between the MainActivity and all fragments
 * */
open class SharedViewModel: ViewModel() {


    /*** Used to notify DevotionalFragment on when to show the AddDevotionalFragment ***/
    val showAddDevotionalFragment = MutableLiveData<Boolean>().apply {
        value = false
    }

    /*** Used to notify VideoFragment on when to show the AddVideoFragment ***/
    val showAddVideoFragment = MutableLiveData<Boolean>().apply {
        value = false
    }


    /*** Used to control the top menu items depending on thw Fragment in view ***/
    val topMenuController = MutableLiveData<Fragments>().apply {
        value = null
    }

    val startSignInProcess = MutableLiveData<Boolean>()


    /****
     *Returns a LiveData User object that all Fragments can observe
     */
    fun getUser(context: Context, id: Int) = UserRepository(
        context
    ).getUser(id)



    /** Choose authentication providers **/
    fun getAuthProviders() = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build())



    fun logout(context: Context) = UserRepository(
        context
    ).logout()


    /**
     * Used to know whether the user has signed out
     **/
    fun listenForUserSignOut(context: Context) = BaseRepository(context).listenForUserSignOut {
        if (!it){//user has Signed Out
            //delete user from db:
            UserRepository(context)
                .deleteUser(viewModelScope, 1)
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
            //But first, we need to know if this user is suppose to be an admin:
            val adminRepository = AdminRepository(context)
            adminRepository.fetchAllAdmin { success, result ->
                if (success) {
                    //search through the list of admin and update the user if his email is on the list:
                    saveUser(context, viewModelScope, 1, user)
                    updateUserAdminStatus(context, user!!, result, viewModelScope)
                    saveSuccessful.invoke(true)
                }
            }
        }else{//this was a sign in operation because the user already existed, so:

            //fetch the user's details from Firestore
            UserRepository(context).getUserFromNetwork(idpResponse.email!!){

                if(it != null){//save the user to local db:
                    //Note: this 'saveUser' will first write the user to Firestore before saving it to local db.
                    //You may be concerned that this will create a duplicate user in Firestore. It will not.
                    //Firestore will simply replace the old document with this new one since they both
                    //have the same ID.
                    //But first, we need to know if this user is suppose to be an admin:
                    val adminRepository = AdminRepository(context)
                    adminRepository.fetchAllAdmin { success, result ->
                        if (success) {
                        //search through the list of admin and update the user if his email is on the list:
                            saveUser(context, viewModelScope, 1, it)
                            updateUserAdminStatus(context, it, result, viewModelScope)
                            saveSuccessful.invoke(true)
                        }
                    }
                }else{
                    saveSuccessful.invoke(false)
                }
            }
        }

    }

    private fun saveUser(context: Context, coroutineScope: CoroutineScope, id: Int, newUser: User) =
        UserRepository(context).saveUser(coroutineScope, id, newUser)

}