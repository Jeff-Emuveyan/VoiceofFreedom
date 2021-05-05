package com.bellogate.voiceoffreedom.model

import androidx.annotation.Keep
import kotlin.properties.Delegates

@Keep
class Admin(){

    lateinit var email: String
    var timeCreated by Delegates.notNull<Long>()

    constructor(email: String, timeCreated: Long): this(){
        this.email = email
        this.timeCreated = timeCreated
        
    }
}