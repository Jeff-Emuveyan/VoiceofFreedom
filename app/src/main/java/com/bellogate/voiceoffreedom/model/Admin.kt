package com.bellogate.voiceoffreedom.model

import androidx.annotation.Keep

@Keep
class Admin(){

    lateinit var email: String
    lateinit var timeCreated: String

    constructor(email: String, timeCreated: String): this(){
        this.email = email
        this.timeCreated = timeCreated
        
    }
}