package com.bellogate.voiceoffreedom.ui.devotional

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bellogate.voiceoffreedom.data.devotional.DevotionalRepository
import com.bellogate.voiceoffreedom.model.Devotional
import com.bellogate.voiceoffreedom.ui.BaseViewModel
import com.bellogate.voiceoffreedom.ui.devotional.util.UIState

class DevotionalViewModel : BaseViewModel() {

    private val _devotional = MutableLiveData<Pair<UIState, Devotional?>>()
    val devotional: LiveData<Pair<UIState, Devotional?>> = _devotional

    fun getDevotionalByDate(context: Context, dateToFind: String) {
        DevotionalRepository(context).getDevotionalByDate(dateToFind) { uiState, devotional ->
            _devotional.value = uiState to devotional
        }
    }


}
