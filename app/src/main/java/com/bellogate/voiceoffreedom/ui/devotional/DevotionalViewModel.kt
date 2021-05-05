package com.bellogate.voiceoffreedom.ui.devotional

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bellogate.voiceoffreedom.data.devotional.DevotionalRepository
import com.bellogate.voiceoffreedom.model.Devotional
import com.bellogate.voiceoffreedom.ui.BaseViewModel
import com.bellogate.voiceoffreedom.ui.devotional.util.DevotionalUIState

class DevotionalViewModel : BaseViewModel() {

    private val _devotional = MutableLiveData<Pair<DevotionalUIState, Devotional?>>()
    val devotional: LiveData<Pair<DevotionalUIState, Devotional?>> = _devotional

    private val _deleteDevotional = MutableLiveData<Boolean>()
    val deleteDevotional : LiveData<Boolean> = _deleteDevotional

    fun getDevotionalByDate(context: Context, dateToFind: String) {
        DevotionalRepository(context).getDevotionalByDate(dateToFind) { uiState, devotional ->
            _devotional.value = uiState to devotional
        }
    }

    fun deleteDevotional(context: Context, devotional: Devotional?) {
        devotional?.let {
            //delete the image file from Storage first:
            DevotionalRepository(context).deleteDevotionalImageFile(it.dateInMilliSeconds){ fileDeleted, _ ->
                if(fileDeleted){
                    DevotionalRepository(context).deleteDevotional(it){ response, _->
                        _deleteDevotional.value = response
                    }
                }else{
                    _deleteDevotional.value = false
                }
            }
        }
    }


}
