package com.bellogate.voiceoffreedom.ui.devotional.add

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import com.bellogate.voiceoffreedom.util.logCollectors

class SyncDevotionalManager {

    companion object{

        var numberOfCollectors: Int = 1 //default

        var listOfCollectors = LinkedHashMap<String, DevotionalCollectorItem>()

        fun syncDevotionals(context: Context, onStart:()-> Unit, invalideInput: (String)-> Unit){
            AlertDialog.Builder(context)
                .setTitle("Upload")
                .setMessage("Upload these devotionals?")
                .setPositiveButton("Yes") { _, _ ->

                    validateCollectors()
                    /*if(validateCollectors()){
                        onStart.invoke()

                    }else{
                        invalideInput.invoke("Missing input field(s)")
                    }*/
                }
                .setNegativeButton("No") { _, _ -> }
                .show()
        }


        private fun validateCollectors(): Boolean{

            logCollectors()

            if(listOfCollectors.isNotEmpty()) {
                for (map in listOfCollectors.entries) {

                }
            }
            return true
        }


    }

}