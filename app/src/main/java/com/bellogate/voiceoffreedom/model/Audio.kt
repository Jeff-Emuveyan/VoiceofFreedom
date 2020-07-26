package com.bellogate.voiceoffreedom.model

import androidx.annotation.Keep

@Keep
class Audio() {

    var title: String? = null
    var audioUrl: String? = null
    var duration: String? = null
    var dateInMilliSeconds: String? = null

    constructor(title: String?,
                 audioUrl: String?,
                 duration: String?,
                 dateInMilliSeconds: String?): this(){

        this.title = title
        this.audioUrl = audioUrl
        this.duration = duration
        this.dateInMilliSeconds = dateInMilliSeconds
    }

}