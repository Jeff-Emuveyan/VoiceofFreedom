package com.bellogate.voiceoffreedom.ui.give.util

class CardInputValidator {

    var isCardNumberValid: Boolean = false
    var isYearValid = false
    var isMonthValid = false
    var isCvvValid = false

    var allInputFieldsValid : Boolean = false
    get() {
        return isCardNumberValid && isYearValid && isMonthValid && isCvvValid
    }
}