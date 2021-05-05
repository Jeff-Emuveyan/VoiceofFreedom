package com.bellogate.voiceoffreedom.model

import androidx.annotation.Keep

@Keep
class Video() {

    var title: String? = null
    var thumbNailUrl: String? = null
    var videoUrl: String? = null
    var duration: String? = null
    var dateInMilliSeconds: String? = null

    constructor(
        title: String?,
        thumbNailUrl: String?,
        videoUrl: String?,
        duration: String?,
        dateInMilliSeconds: String?
    ): this() {
        this.title = title
        this.thumbNailUrl = thumbNailUrl
        this.videoUrl = videoUrl
        this.duration = duration
        this.dateInMilliSeconds = dateInMilliSeconds
    }
}