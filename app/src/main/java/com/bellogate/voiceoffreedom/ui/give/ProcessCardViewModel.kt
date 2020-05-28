package com.bellogate.voiceoffreedom.ui.give

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.paystack.android.Paystack
import co.paystack.android.PaystackSdk
import co.paystack.android.Transaction
import co.paystack.android.model.Card
import co.paystack.android.model.Charge
import com.bellogate.voiceoffreedom.data.setup.UserRepository
import com.bellogate.voiceoffreedom.ui.give.util.CardProcessState
import com.bellogate.voiceoffreedom.util.isStagingBuild

class ProcessCardViewModel : ViewModel() {


    private val _cardProcessState = MutableLiveData<CardProcessState>()
    val cardProcessState : LiveData<CardProcessState> = _cardProcessState

    /****
     *Returns a LiveData User object that all Fragments can observe
     */
    fun getUser(context: Context, id: Int) = UserRepository(context).getUser(id)


    fun processCard(activity: Activity, card: Card, amount: Int, email: String){

        if (card.isValid) { // charge card
            val charge = Charge()
            charge.card = card //sets the card to charge
            charge.amount = amount
            charge.email = email //customer's email

            PaystackSdk.chargeCard(activity, charge, object : Paystack.TransactionCallback {
                override fun onSuccess(transaction: Transaction?) { // This is called only after transaction is deemed successful.
                    // Retrieve the transaction, and send its reference to your server
                    // for verification.
                    _cardProcessState.value = CardProcessState.SUCCESS
                }

                override fun beforeValidate(transaction: Transaction?) { // This is called only before requesting OTP.
                    // Save reference so you may send to server. If
                    // error occurs with OTP, you should still verify on server.
                    _cardProcessState.value = CardProcessState.OTP_SENT
                }

                override fun onError(error: Throwable?, transaction: Transaction?) {
                    //handle error here
                    _cardProcessState.value = CardProcessState.FAILED
                }
            })

        }else{
            _cardProcessState.value = CardProcessState.INVALID_CARD
        }
    }

    fun setUpPayStack(context: Context, key: String){
        PaystackSdk.initialize(context)

        PaystackSdk.setPublicKey(key);
    }
}
