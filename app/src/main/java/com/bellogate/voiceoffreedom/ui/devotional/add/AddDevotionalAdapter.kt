package com.bellogate.voiceoffreedom.ui.devotional.add

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bellogate.voiceoffreedom.R
import com.bellogate.voiceoffreedom.data.devotional.SyncMultipleDevotionalsManager
import com.bellogate.voiceoffreedom.util.deleteCollectedItem
import com.bellogate.voiceoffreedom.util.getBitmapFromUri
import com.bellogate.voiceoffreedom.util.selectImage
import com.bellogate.voiceoffreedom.util.updateCollectedItems

class AddDevotionalAdapter private constructor(): RecyclerView.Adapter<DevotionalCollectorItem>() {

    var numberOfCollectorsToShow: Int = 1 //default
    set(value) {
        field = value
        SyncMultipleDevotionalsManager.numberOfCollectors = field
    }
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
            selectImage(activity){uri, file ->
                holder.imageUri = uri
                holder.imageView.setImageBitmap(getBitmapFromUri(activity, holder.imageUri!!))
                updateCollectedItems(holder)
            }
        }

        holder.ivCancel.setOnClickListener {
            deleteAlert(activity, holder){
                if(it){
                    holder.clearData()
                }
            }
        }
    }


    private fun deleteAlert(context: Context, devotionalCollectorItem: DevotionalCollectorItem,
                            deleted: (Boolean)-> Unit){
        AlertDialog.Builder(context)
            .setTitle("Remove")
            .setMessage("Remove this devotional?")
            .setPositiveButton("Yes") { _, _ ->

                deleteCollectedItem(devotionalCollectorItem)

                numberOfCollectorsToShow -= 1
                this.notifyDataSetChanged()

                deleted.invoke(true)
            }
            .setNegativeButton("No") { _, _ -> }
            .show()
    }

}