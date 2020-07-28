package com.bellogate.voiceoffreedom.model

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bellogate.voiceoffreedom.R
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout

class ListItem(v: View): RecyclerView.ViewHolder(v)  {

    lateinit var itemLayout: ConstraintLayout
    lateinit var shimmer: ShimmerFrameLayout
    lateinit var ivThumbnail: ImageView
    lateinit var ivDeleteVideo: ImageView
    lateinit var tvTitle: TextView
    lateinit var tvDuration: TextView
    lateinit var tvDate: TextView
    lateinit var view: View
    lateinit var context: Context


    constructor(context: Context, v: View): this(v) {
        this.context = context
        this.view = v
        shimmer = view.findViewById(R.id.shimmer)
        itemLayout = view.findViewById(R.id.layout)
        ivThumbnail = view.findViewById(R.id.ivThumbnail)
        tvTitle = view.findViewById(R.id.tvTitle)
        tvDuration = view.findViewById(R.id.tvDuration)
        tvDate = view.findViewById(R.id.tvDate)
        ivDeleteVideo = view.findViewById(R.id.ivCancel)
    }
}