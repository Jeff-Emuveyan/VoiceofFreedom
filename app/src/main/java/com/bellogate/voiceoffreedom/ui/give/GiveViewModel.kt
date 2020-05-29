package com.bellogate.voiceoffreedom.ui.give

import android.content.Context
import androidx.lifecycle.ViewModel
import com.bellogate.voiceoffreedom.data.KeyRepository
import com.bellogate.voiceoffreedom.ui.SharedViewModel

class GiveViewModel : SharedViewModel() {

    fun fetchKey(context: Context, response:(success: Boolean, key: String?)-> Unit){
        KeyRepository(context).fetchKey{ success, key ->
            response.invoke(success, key?.publicKey)
        }
    }
}
