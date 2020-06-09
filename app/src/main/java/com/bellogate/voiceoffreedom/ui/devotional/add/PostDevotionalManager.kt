package com.bellogate.voiceoffreedom.ui.devotional.add

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.View

class PostDevotionalManager {

    companion object{
        fun syncDevotionals(context: Context, onStart:()-> Unit, invalideInput: (String)-> Unit){
            AlertDialog.Builder(context)
                .setTitle("Upload")
                .setMessage("Upload these devotionals?")
                .setPositiveButton("Yes") { _, _ ->

                    if(validateCollectors()){
                        onStart.invoke()

                    }else{
                        invalideInput.invoke("Missing input field(s)")
                    }
                }
                .setNegativeButton("No") { _, _ -> }
                .show()
        }


        fun validateCollectors(): Boolean{

            return true
        }


    }

}