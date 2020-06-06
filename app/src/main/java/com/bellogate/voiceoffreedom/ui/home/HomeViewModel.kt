package com.bellogate.voiceoffreedom.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bellogate.voiceoffreedom.ui.BaseViewModel
import com.bellogate.voiceoffreedom.ui.SharedViewModel

class HomeViewModel : BaseViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text
}