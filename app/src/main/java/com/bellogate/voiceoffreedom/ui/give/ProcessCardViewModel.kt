package com.bellogate.voiceoffreedom.ui.give

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.paystack.android.Paystack
import co.paystack.android.PaystackSdk
import co.paystack.android.Transaction
import co.paystack.android.model.Card
import co.paystack.android.model.Charge
import com.bellogate.voiceoffreedom.data.UserRepository
import com.bellogate.voiceoffreedom.ui.BaseViewModel
import com.bellogate.voiceoffreedom.ui.give.util.CardProcessState

class ProcessCardViewModel : BaseViewModel() {


    private val _cardProcessState = MutableLiveData<Pair<CardProcessState, String?>>()
    val cardProcessState : LiveData<Pair<CardProcessState, String?>> = _cardProcessState


    fun processCard(activity: Activity, card: Card, amount: Int, email: String){

        if (card.isValid) { // charge card
            val charge = Charge()
            charge.card = card //sets the card to charge
            charge.amount = amount * 100// converting from KOBO to naira
            charge.email = email //customer's email

            PaystackSdk.chargeCard(activity, charge, object : Paystack.TransactionCallback {
                override fun onSuccess(transaction: Transaction?) { // This is called only after transaction is deemed successful.
                    // Retrieve the transaction, and send its reference to your server
                    // for verification.
                    _cardProcessState.value = CardProcessState.SUCCESS to null
                }

                override fun beforeValidate(transaction: Transaction?) { // This is called only before requesting OTP.
                    // Save reference so you may send to server. If
                    // error occurs with OTP, you should still verify on server.
                    _cardProcessState.value = CardProcessState.OTP_SENT to null
                }

                override fun onError(error: Throwable?, transaction: Transaction?) {
                    //handle error here
                    _cardProcessState.value = CardProcessState.FAILED to error?.message
                }
            })

        }else{
            _cardProcessState.value = CardProcessState.INVALID_CARD to "Invalid card"
        }
    }

    fun setUpPayStack(context: Context, key: String){
        PaystackSdk.initialize(context)

        PaystackSdk.setPublicKey(key);
    }
}
