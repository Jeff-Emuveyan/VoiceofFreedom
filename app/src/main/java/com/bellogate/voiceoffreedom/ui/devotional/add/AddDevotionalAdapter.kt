package com.bellogate.voiceoffreedom.ui.devotional.add

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bellogate.voiceoffreedom.R

class AddDevotionalAdapter private constructor(): RecyclerView.Adapter<DevotionalItem>() {

    private lateinit var context: Context
    private var itemCount: Int = 1 //default

    constructor(context: Context, itemCount: Int): this(){
        this.context = context
        this.itemCount = itemCount
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevotionalItem {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.add_devotional_item, parent, false)
        return DevotionalItem(context!!, view)
    }

    override fun getItemCount(): Int {
        return itemCount
    }

    override fun onBindViewHolder(holder: DevotionalItem, position: Int) {

        holder.imageView.setOnClickListener {
        }

        holder.ivCancel.setOnClickListener {
        }
    }
}