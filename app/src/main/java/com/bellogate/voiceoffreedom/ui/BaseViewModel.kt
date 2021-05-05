package com.bellogate.voiceoffreedom.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import com.bellogate.voiceoffreedom.data.UserRepository

abstract class BaseViewModel: ViewModel() {

    /****
     *Returns a LiveData User object that all Fragments can observe
     */
    fun getUser(context: Context, id: Int) = UserRepository(
        context
    ).getUser(id)
}