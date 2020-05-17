package com.bellogate.voiceoffreedom.ui.setup

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bellogate.voiceoffreedom.data.datasource.network.NetworkHelper
import com.bellogate.voiceoffreedom.data.setup.SetupRepository
import com.bellogate.voiceoffreedom.data.setup.SetupState
import com.bellogate.voiceoffreedom.model.Admin
import com.bellogate.voiceoffreedom.model.User
import kotlinx.coroutines.launch

class SetupActivityViewModel: ViewModel() {

    private val _setUpState = MutableLiveData<SetupState>()
    val setUpState : LiveData<SetupState> = _setUpState


    /**
     * The aim of this method is to check if there is a user on the device,
     * and if this user should be an Admin or not. By default, a User is not an admin.
     *
     * This method will check if there is a user on the device.
     * If there is, it will fetch all admin from Firebase and check if the email of the user
     * on the device is among that list. If there is no user on the device, nothing happens.
     * */
    fun checkAndUpdateUserStatus(context: Context) = viewModelScope.launch {
        val repository = SetupRepository(context)
        //check if there is a user on the local database:
        val user = repository.getUserSynchronously(1)
        if(user != null) {
            repository.fetchAllAdmin { success, result ->
                if (success) {
                    //search through the list of admin and update the user if his email is on the list
                    updateUser(context, user, result)
                } else {
                    _setUpState.postValue(SetupState.NETWORK_ERROR)
                }
            }
        }else{
            _setUpState.postValue(SetupState.COMPLETE)
        }
    }


    private fun updateUser(context: Context, user: User, adminList: ArrayList<Admin>?) = viewModelScope.launch{
        val repository = SetupRepository(context)

        adminList?.let {
            for((_, email) in it){
                if(user.email == email){//update the user to an admin
                    user.isAdmin = true
                    repository.updateUser(user)
                }else{//this means that the present user should not be an admin
                    user.isAdmin = false
                    repository.updateUser(user)
                }
            }
        }
    }


}




