package com.bellogate.voiceoffreedom.ui.setup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bellogate.voiceoffreedom.ui.MainActivity
import com.bellogate.voiceoffreedom.R
import com.bellogate.voiceoffreedom.data.setup.SetupState
import kotlinx.android.synthetic.main.activity_setup.*

class SetupActivity : AppCompatActivity() {

    private lateinit var setupActivityViewModel: SetupActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        tvNetworkFailure.visibility = View.INVISIBLE
        tvNetworkFailure.setOnClickListener{
            checkAndUpdateUserAdminStatus()
        }

        setupActivityViewModel = ViewModelProviders.of(this).get(SetupActivityViewModel::class.java)
        setupActivityViewModel.setUpState.observe(this, Observer {

            when (it){
                SetupState.COMPLETE ->{
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }

                SetupState.NETWORK_ERROR ->{
                    tvNetworkFailure.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                }
            }
        })


        checkAndUpdateUserAdminStatus()
    }

    private fun checkAndUpdateUserAdminStatus() {
        tvNetworkFailure.visibility = View.INVISIBLE
        progressBar.visibility = View.VISIBLE
        setupActivityViewModel.checkAndUpdateUserAdminStatus(this)
    }
}
