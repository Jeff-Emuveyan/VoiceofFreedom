package com.bellogate.voiceoffreedom.ui.setup

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bellogate.voiceoffreedom.data.setup.AdminRepository
import com.bellogate.voiceoffreedom.data.setup.SetupState
import com.bellogate.voiceoffreedom.data.setup.UserRepository
import com.bellogate.voiceoffreedom.model.Admin
import com.bellogate.voiceoffreedom.model.User
import com.bellogate.voiceoffreedom.ui.SharedViewModel
import kotlinx.coroutines.launch

class SetupActivityViewModel: SharedViewModel() {

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
    fun checkAndUpdateUserAdminStatus(context: Context) = viewModelScope.launch {
        val repository = UserRepository(context)
        val adminRepository = AdminRepository(context)
        //check if there is a user on the local database:
        val user = repository.getUserSynchronously(1)
        if(user != null) {
            adminRepository.fetchAllAdmin { success, result ->
                if (success) {
                    //search through the list of admin and update the user if his email is on the list
                    updateUserAdminStatus(context, user!!, result)
                    _setUpState.postValue(SetupState.COMPLETE)
                } else {
                    _setUpState.postValue(SetupState.NETWORK_ERROR)
                }
            }
        }else{
            _setUpState.postValue(SetupState.COMPLETE)
        }
    }


    /**
     * This will update the user's admin status in Firebase and Room DB
     */
    private fun updateUserAdminStatus(context: Context, user: User, adminList: ArrayList<Admin>?){
        val repository = UserRepository(context)

        adminList?.let {
            for(admin in it){
                if(admin.email == user.email){//update the user to an admin
                    user.isAdmin = true
                    repository.updateUser(viewModelScope, user)
                }else{//this means that the present user should not be an admin
                    user.isAdmin = false
                    repository.updateUser(viewModelScope, user)
                }
            }
        }
    }


}




