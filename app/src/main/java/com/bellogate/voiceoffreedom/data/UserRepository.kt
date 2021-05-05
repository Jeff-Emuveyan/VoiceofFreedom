package com.bellogate.voiceoffreedom.data

import android.content.Context
import androidx.lifecycle.LiveData
import com.bellogate.voiceoffreedom.data.BaseRepository
import com.bellogate.voiceoffreedom.data.datasource.network.NetworkHelper
import com.bellogate.voiceoffreedom.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class UserRepository(context: Context) : BaseRepository(context){

    /***fetch a user asynchronously ie returns a Livedata so there is no need to make it suspend.
     * It is already asynchronous**/
    fun getUser(id: Int): LiveData<User?> = db.userDao().getUser(id)


    /***fetch a user synchronously ie does not return Livedata. Room won't let us read data synchronously,
     *So we must either return a LiveData which will do the async work or make this function suspend**/
    suspend fun getUserSynchronously(id: Int) = db.userDao().getUserSynchronously(id)


    /** Fetch a user from network **/
    fun getUserFromNetwork(userEmail: String, user: (User?) -> Unit) =
        NetworkHelper.getUser(userEmail){
            if(it != null){
                user.invoke(it)
            }else{
                user.invoke(null)
            }
        }



    /** Updates User to Firebase and later to Room database **/
    fun updateUser(coroutineScope: CoroutineScope, user: User){
        NetworkHelper.syncUser(user) {
            if (it) {
                coroutineScope.launch {
                    db.userDao().updateUser(user)
                }
            }
        }
    }


    /** Saves user to Firebase and later to Room database **/
    fun saveUser(coroutineScope: CoroutineScope, id: Int, newUser: User){
        NetworkHelper.syncUser(newUser){
            if (it){
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
    }


    /***delete**/
    fun deleteUser(coroutineScope: CoroutineScope, id: Int){
        coroutineScope.launch {
            val user = getUserSynchronously(id)
            if (user != null){
                db.userDao().deleteUser(user)
            }
        }
    }
}