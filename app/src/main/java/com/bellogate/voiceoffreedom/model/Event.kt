package com.bellogate.voiceoffreedom.model

import androidx.annotation.Keep

@Keep
class Event(){

    var imageUrl: String? = null

    constructor(imageUrl: String): this(){
        this.imageUrl = imageUrl
    }
}