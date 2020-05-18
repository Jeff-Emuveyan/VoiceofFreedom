package com.bellogate.voiceoffreedom.data

import android.content.Context
import androidx.lifecycle.LiveData
import com.bellogate.voiceoffreedom.data.datasource.database.AppDatabase
import com.bellogate.voiceoffreedom.model.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

open class BaseRepository(val context: Context) {

    val db = AppDatabase.getDatabase(context)

    /***fetch a user asynchronously ie returns a Livedata so there is no need to make it suspend.
     * It is already asynchronous**/
    fun getUser(id: Int): LiveData<User> = db.userDao().getUser(id)

    /***fetch a user synchronously ie does not return Livedata. Room won't let us read data synchronously,
     *So we must either return a LiveData which will do the async work or make this function suspend**/
    suspend fun getUserSynchronously(id: Int) = db.userDao().getUserSynchronously(id)


    suspend fun updateUser(user: User) = db.userDao().updateUser(user)


    /** Saves user to Room database **/
    fun saveUser(coroutineScope: CoroutineScope, id: Int, newUser: User){
        coroutineScope.launch {
            val oldUser = getUserSynchronously(id)
            if (oldUser == null){
                db.userDao().saveUser(newUser)
            }else{
                db.userDao().updateUser(newUser)
            }

        }
    }

}