package com.bellogate.voiceoffreedom.ui.devotional.add

import androidx.lifecycle.ViewModel

class AddDevotionalViewModel : ViewModel() {


    /** For this version, we only allow the user to add a maximum number of 4 collectors.
     * This is so that we won't need to manage the state of the recycler items when the user has
     * scrolled down a long list.
     */
    fun maximumNumberOfCollectorsReached(currentNumber: Int): Boolean{
        return (currentNumber == 4)
    }

}
