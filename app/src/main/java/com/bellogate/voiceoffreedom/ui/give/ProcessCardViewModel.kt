package com.bellogate.voiceoffreedom.ui.give

import android.content.Context
import androidx.lifecycle.ViewModel
import co.paystack.android.PaystackSdk
import com.bellogate.voiceoffreedom.data.setup.UserRepository
import com.bellogate.voiceoffreedom.util.isStagingBuild

class ProcessCardViewModel : ViewModel() {


    /****
     *Returns a LiveData User object that all Fragments can observe
     */
    fun getUser(context: Context, id: Int) = UserRepository(context).getUser(id)



    fun setUpPayStack(context: Context, key: String){
        PaystackSdk.initialize(context)

        PaystackSdk.setPublicKey(key);
    }
}
