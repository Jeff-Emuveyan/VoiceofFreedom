package com.bellogate.voiceoffreedom.ui.give.util

import co.paystack.android.model.Card

class CardInputValidator {

    var isCardNumberValid: Boolean = false
    var isYearValid = false
    var isMonthValid = false
    var isCvvValid = false

    var allInputFieldsValid : Boolean = false
    get() {
        return isCardNumberValid && isYearValid && isMonthValid && isCvvValid
    }

    var cardNumber: String = ""
    var year = ""
    var month = ""
    var cvv = ""

    fun getCard() = Card(this.cardNumber, this.month.toInt(), this.year.toInt(), this.cvv)
}