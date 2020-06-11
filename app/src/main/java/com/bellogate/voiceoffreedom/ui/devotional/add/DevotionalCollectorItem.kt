package com.bellogate.voiceoffreedom.ui.devotional.add

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bellogate.voiceoffreedom.R
import com.bellogate.voiceoffreedom.util.getSimpleDateFormat
import com.bellogate.voiceoffreedom.util.showDatePickerDialog
import com.bellogate.voiceoffreedom.util.updateCollectedItems
import com.google.android.material.button.MaterialButton
import java.util.*

class DevotionalCollectorItem private constructor(v: View): RecyclerView.ViewHolder(v) ,
    DatePickerDialog.OnDateSetListener{

    lateinit var tvInstruction: TextView
    lateinit var imageView: ImageView
    lateinit var ivCancel: ImageView
    lateinit var buttonDate: MaterialButton
    lateinit var context: Context

    //used to create devotionals:
    var id: String? = null
    var dateInMillis: Long? = null
    var dateInSimpleForm : String? = null
    var imageUri: Uri? = null

    constructor(context: Context, v: View): this(v){
        this.context = context
        id = UUID.randomUUID().toString()
        tvInstruction = v.findViewById(R.id.tvInstruction)
        imageView = v.findViewById(R.id.imageView)
        ivCancel = v.findViewById(R.id.ivCancel)
        buttonDate = v.findViewById(R.id.buttonDate)

        buttonDate.setOnClickListener{
            showDatePickerDialog(context,false, this)
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)

        val realMonth = month + 1
        val dateInSimpleFormat = "$year-$realMonth-$dayOfMonth" //ie 30-04-1994

        dateInMillis = calendar.timeInMillis
        dateInSimpleForm = getSimpleDateFormat(dateInMillis!!, "dd-MMM-yyyy")//ie 30-APR-1994

        buttonDate.text = dateInSimpleForm
        updateCollectedItems(this)
    }

    override fun toString(): String {
        return "$dateInMillis, $dateInSimpleForm, ${imageUri?.path ?: "No image"}"
    }


    fun clearData(){
        dateInMillis = null
        dateInSimpleForm  = null
        imageUri = null

        imageView.setImageResource(R.drawable.ic_insert_photo)
        buttonDate.setText(R.string.select_date)

    }
}