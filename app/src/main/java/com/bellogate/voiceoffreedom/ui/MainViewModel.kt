package com.bellogate.voiceoffreedom.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import com.bellogate.voiceoffreedom.data.BaseRepository
import com.bellogate.voiceoffreedom.model.User
import com.firebase.ui.auth.AuthUI
import kotlinx.coroutines.CoroutineScope

open class MainViewModel: ViewModel() {

    /** Choose authentication providers **/
    fun getAuthProviders() = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build())


    fun saveUser(context: Context, coroutineScope: CoroutineScope, id: Int, newUser: User) =
        BaseRepository(context).saveUser(coroutineScope, id, newUser)



}