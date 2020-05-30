package com.bellogate.voiceoffreedom.util

import android.content.Context
import android.content.DialogInterface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bellogate.voiceoffreedom.BuildConfig
import com.bellogate.voiceoffreedom.R
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar


const val AMOUNT = "amount"

fun showSnackMessage(view: View, message: String){
    Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
}

fun showSnackMessageAtTop(context: Context, view: View, message: String){
    val snackBarView = Snackbar.make(view, message , Snackbar.LENGTH_LONG)
    snackBarView.duration = 10000
    val view = snackBarView.view
    val params = view.layoutParams as FrameLayout.LayoutParams
    params.gravity = Gravity.TOP
    view.layoutParams = params
    view.background = ContextCompat.getDrawable(context, R.color.green) // for custom background
    snackBarView.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
    snackBarView.show()
}


fun isStagingBuild(): Boolean {
    return BuildConfig.FLAVOR == "staging"
}

fun isProductionBuild(): Boolean {
    return BuildConfig.FLAVOR == "production"
}



fun Fragment.showAlertForSuccessfulPayment(){

    val layout = LayoutInflater.from(this.requireContext())

    val view: View = layout.inflate(R.layout.alert_payment_successful_layout, null)

    val alertBuilder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())

    alertBuilder.setView(view)
    alertBuilder.setCancelable(false)
    val alert = alertBuilder.show()

    val okButton = view.findViewById<Button>(R.id.okButton)
    okButton.setOnClickListener{
        alertBuilder.setCancelable(true)
        alert.dismiss()
        findNavController().popBackStack()
        findNavController().popBackStack()
    }

}


fun Fragment.showAlert(title: String, message: String){
    val alertBuilder: AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
    alertBuilder.setTitle(title)
    alertBuilder.setMessage(message)
    alertBuilder.setPositiveButton("Close"
    ) { _, _ -> }
    alertBuilder.show()
}



