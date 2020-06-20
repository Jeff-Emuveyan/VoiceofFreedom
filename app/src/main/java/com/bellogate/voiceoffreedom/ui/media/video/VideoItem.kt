package com.bellogate.voiceoffreedom.ui.media.video

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bellogate.voiceoffreedom.R

class VideoItem(v: View): RecyclerView.ViewHolder(v)  {

    lateinit var ivThumbnail: ImageView
    lateinit var tvTitle: TextView
    lateinit var tvDuration: TextView
    lateinit var view: View
    lateinit var context: Context


    constructor(context: Context, v: View): this(v) {
        this.context = context
        this.view = v
        ivThumbnail = view.findViewById(R.id.ivThumbnail)
        tvTitle = view.findViewById(R.id.tvTitle)
        tvDuration = view.findViewById(R.id.tvDuration)
    }
}