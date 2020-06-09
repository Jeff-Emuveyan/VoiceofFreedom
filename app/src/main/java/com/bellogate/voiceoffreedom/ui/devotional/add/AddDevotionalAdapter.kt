package com.bellogate.voiceoffreedom.ui.devotional.add

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bellogate.voiceoffreedom.R
import com.bellogate.voiceoffreedom.util.getBitmapFromUri
import com.bellogate.voiceoffreedom.util.selectImage

class AddDevotionalAdapter private constructor(): RecyclerView.Adapter<DevotionalCollectorItem>() {

    var numberOfCollectorsToShow: Int = 1 //default
    private lateinit var activity: FragmentActivity

    constructor(activity: FragmentActivity, numberOfCollectorsToShow: Int): this(){
        this.activity = activity
        this.numberOfCollectorsToShow = numberOfCollectorsToShow
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevotionalCollectorItem {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.add_devotional_item, parent, false)
        return DevotionalCollectorItem(activity, view)
    }

    override fun getItemCount(): Int {
        return numberOfCollectorsToShow
    }

    override fun onBindViewHolder(holder: DevotionalCollectorItem, position: Int) {

        holder.imageView.setOnClickListener {
            selectImage(activity){uri, filePath ->
                holder.imageUri = uri
                holder.imageView.setImageBitmap(getBitmapFromUri(activity, holder.imageUri!!))
            }
        }

        holder.ivCancel.setOnClickListener {
        }
    }



}