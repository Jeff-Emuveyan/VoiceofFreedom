package com.bellogate.voiceoffreedom.ui.devotional.add

import android.app.DatePickerDialog
import android.content.Context
import android.view.View
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bellogate.voiceoffreedom.R
import com.bellogate.voiceoffreedom.util.getSimpleDateFormat
import com.bellogate.voiceoffreedom.util.showDatePickerDialog
import com.google.android.material.button.MaterialButton
import java.util.*

class DevotionalItem private constructor(v: View): RecyclerView.ViewHolder(v) ,
    DatePickerDialog.OnDateSetListener{

    lateinit var tvInstruction: TextView
    lateinit var imageView: ImageView
    lateinit var ivCancel: ImageView
    lateinit var buttonDate: MaterialButton
    lateinit var context: Context

    var dateInSimpleForm : String? = null
    var dateInMillis: Long? = null

    constructor(context: Context, v: View): this(v){
        this.context = context
        tvInstruction = v.findViewById(R.id.tvInstruction)
        imageView = v.findViewById(R.id.imageView)
        ivCancel = v.findViewById(R.id.ivCancel)
        buttonDate = v.findViewById(R.id.buttonDate)

        buttonDate.setOnClickListener{
            showDatePickerDialog(context,this)
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)

        val realMonth = month + 1
        val dateInSimpleFormat = "$year-$realMonth-$dayOfMonth"

        dateInMillis = calendar.timeInMillis
        dateInSimpleForm = getSimpleDateFormat(dateInMillis!!, "dd-MMM-yyyy")

        buttonDate.text = dateInSimpleForm

    }
}