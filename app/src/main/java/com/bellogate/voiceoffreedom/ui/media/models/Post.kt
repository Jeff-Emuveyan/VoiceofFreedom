package com.bellogate.voiceoffreedom.ui.media.models

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Post(
    var title: String? = null,
    var duration: String? = null,
    var thumbnailUrl: String? = null,
    var url: String? = null
)