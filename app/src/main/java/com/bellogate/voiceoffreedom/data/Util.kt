package com.bellogate.voiceoffreedom.data

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.bellogate.voiceoffreedom.R
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

/**
 *collection names
 */
const val ADMIN = "admin"


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
