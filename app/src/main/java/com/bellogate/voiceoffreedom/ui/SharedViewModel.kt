package com.bellogate.voiceoffreedom.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bellogate.voiceoffreedom.data.BaseRepository
import com.bellogate.voiceoffreedom.model.User
import com.firebase.ui.auth.AuthUI
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
    fun getUser(context: Context, id: Int) = BaseRepository(context).getUser(id)


    fun saveUser(context: Context, coroutineScope: CoroutineScope, id: Int, newUser: User) =
        BaseRepository(context).saveUser(coroutineScope, id, newUser)


    fun logout(context: Context) = BaseRepository(context).logout()


    /**
     * Used to know whether the user has signed out
     **/
    fun listenForUserSignOut(context: Context) = BaseRepository(context).listenForUserSignOut {
        if (!it){//user has Signed Out
            //delete user from db:
            BaseRepository(context).deleteUser(viewModelScope, 1)
        }
    }


}