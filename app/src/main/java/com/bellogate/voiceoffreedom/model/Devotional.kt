package com.bellogate.voiceoffreedom.model

import androidx.annotation.Keep

@Keep
class Devotional() {

    lateinit var dateInMilliSeconds: String
    lateinit var dateInSimpleFormat: String
    lateinit var bitmapUrlLink: String

    constructor(dateInMilliSeconds: String, dateInSimpleFormat: String, bitmapUrlLink: String): this(){
        this.dateInMilliSeconds = dateInMilliSeconds
        this.dateInSimpleFormat = dateInSimpleFormat
        this.bitmapUrlLink = bitmapUrlLink

    }

}