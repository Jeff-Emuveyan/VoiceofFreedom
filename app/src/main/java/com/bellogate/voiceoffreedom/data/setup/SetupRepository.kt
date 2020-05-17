package com.bellogate.voiceoffreedom.data.setup

import android.content.Context
import com.bellogate.voiceoffreedom.data.Repository
import com.bellogate.voiceoffreedom.data.datasource.network.NetworkHelper
import com.bellogate.voiceoffreedom.model.Admin

class SetupRepository(context: Context): Repository(context) {

    /**
     * The aim of this method is to check if there is a user on the device,
     * and if this user should be an Admin or not. By default, a User is not an admin.
     * */
    fun fetchAllAdmin(fetched:(Boolean, ArrayList<Admin>?) -> Unit) =
            NetworkHelper.fetchAllAdmin { success, result ->
                fetched.invoke(success, result)
            }

}