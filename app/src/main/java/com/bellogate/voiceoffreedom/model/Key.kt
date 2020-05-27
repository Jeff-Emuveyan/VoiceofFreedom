package com.bellogate.voiceoffreedom.model

import androidx.annotation.Keep
import java.security.PublicKey

@Keep
class Key() {

    lateinit var publicKey: String
    lateinit var secretKey: String

    constructor(publicKey: String, secretKey: String): this(){
        this.publicKey = publicKey
        this.secretKey = secretKey
    }
}