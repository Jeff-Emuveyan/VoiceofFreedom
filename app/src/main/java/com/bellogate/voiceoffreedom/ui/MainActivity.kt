package com.bellogate.voiceoffreedom.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bellogate.voiceoffreedom.R
import com.bellogate.voiceoffreedom.data.devotional.SyncDevotionalsReceiver
import com.bellogate.voiceoffreedom.model.User
import com.bellogate.voiceoffreedom.util.*
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.navigation.NavigationView


const val  RC_SIGN_IN = 44

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var drawerLayout: DrawerLayout
    private var user: User? = null
    private lateinit var broadcastReceiver: SyncDevotionalsReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_home,
            R.id.nav_give,
            R.id.nav_devotional,
            R.id.nav_video,
            R.id.nav_about
        ), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        toolbar.setTitleTextColor(resources.getColor(R.color.black))
        toolbar.setTitleTextAppearance(this,
            R.style.LatoBoldTextAppearance
        )//change the font

        registerBroadcastReceiver()

        sharedViewModel = ViewModelProviders.of(this).get(SharedViewModel::class.java)

        //we place a constant listener to know when the user has signed out,
        // So that we can know when to delete the user from db
        //This will trigger anytime the user sign out. Successfully or not.
        sharedViewModel.listenForUserSignOut(this)


        sharedViewModel.getUser(this, 1).observe(this, Observer {
            //anytime the user has logged in or out, we regulate the menu to show the right items:
            user = it
            invalidateOptionsMenu()//this will cause 'onCreateOptionsMenu' to run again
        })

        sharedViewModel.startSignInProcess.observe(this, Observer {
            if(it){
                launchFirebaseAuthentication()
            }
        })

        sharedViewModel.topMenuController.observe(this, Observer {
            //anytime there is a call to change items in the top menu:
            invalidateOptionsMenu()//this will cause 'onCreateOptionsMenu' to run again
        })


        //check for updates:
        ClientUpdateManager.init(this)
        ClientUpdateManager.requireUpdate()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        if(user == null){
            //when the app newly starts, the logout menu option should be hidden
            // but the log in option should be visible:

            //Hide the logOut menu
            val menuItem = menu.findItem(R.id.logout)
            menuItem.isVisible = false

            //Show login menu
            val menuLogIn = menu.findItem(R.id.sign_up)
            menuLogIn.isVisible = true

        }else{
            //if there is a user, the user should only be able to logout:
            //Hide the login menu
            val menuItem = menu.findItem(R.id.logout)
            menuItem.isVisible = true

            //Hide login menu
            val menuLogIn = menu.findItem(R.id.sign_up)
            menuLogIn.isVisible = false

            //Determines when to show user admin permissions to Add Devotionals
            menu.findItem(R.id.add_devotional).isVisible =
                sharedViewModel.topMenuController.value == Fragments.DEVOTIONAL

            //Determines when to show user admin permissions to Add video
            menu.findItem(R.id.add_video).isVisible =
                sharedViewModel.topMenuController.value == Fragments.VIDEO

            //Determines when to show user admin permissions to Change event image
            menu.findItem(R.id.change_event).isVisible =
                sharedViewModel.topMenuController.value == Fragments.HOME
        }

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId){
            R.id.sign_up ->{
                launchFirebaseAuthentication()
            }
            R.id.logout ->{
                logout()
            }
            R.id.add_devotional ->{
                sharedViewModel.showAddDevotionalFragment.value = true
            }
            R.id.add_video ->{
                sharedViewModel.showAddVideoFragment.value = true
            }
            R.id.change_event ->{
                sharedViewModel.launchGallery.value = true
            }
            else ->{
                drawerLayout.openDrawer(Gravity.LEFT)
            }
        }
        return true
    }

    private fun logout() = sharedViewModel.logout(this)


    /**
     * Uses firebase UI auth to sign up a user
     * ***/
    private fun launchFirebaseAuthentication() {
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(sharedViewModel.getAuthProviders())
                .build(),
            RC_SIGN_IN
        )
        sharedViewModel.startSignInProcess.value = false
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            handleSignIn(resultCode, data)
        }

        if (requestCode == ClientUpdateManager.UPDATE_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) { // If the update is cancelled or fails,
                // you can request to start the update again.
                Toast.makeText(this, R.string.update_stopped, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, R.string.complete, Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun handleSignIn(resultCode : Int, data: Intent?) {
        val response = IdpResponse.fromResultIntent(data)
        val view = this.window?.decorView?.rootView

        if (resultCode == Activity.RESULT_OK && response != null) {

            val progressDialog = ProgressDialog(this@MainActivity)
            progressDialog.setTitle("Please wait....")
            progressDialog.show()
            sharedViewModel.handleSuccessfulSignIn(this, response){
                if(it){
                    showSnackMessageAtTop(this, view!!, "Login successful!!")
                }else{
                  Toast.makeText(this, "Try again...", Toast.LENGTH_LONG).show()
                }
                progressDialog.dismiss()
            }


        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            handleSignInError(response)
        }
    }


    @SuppressLint("RestrictedApi")
    private fun handleSignInError(response : IdpResponse?) {
        val view = this.window?.decorView?.rootView
        if (response == null){
            showSnackMessage(
                view!!,
                "Cancelled"
            )
        }else{
            showSnackMessage(
                view!!,
                ErrorCodes.toFriendlyMessage(response.error!!.errorCode)
            )
        }
    }


    private fun registerBroadcastReceiver(){

        broadcastReceiver = SyncDevotionalsReceiver()
        //Intent filters specify the types of intents a component can receive.
        //They are used in filtering out the intents based on Intent values like action.
        val filter = IntentFilter()
        filter.addAction(STOP_NOTIFICATION)
        registerReceiver(broadcastReceiver, filter)
    }


    override fun onDestroy() {
        unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        ClientUpdateManager.listenForUpdateStateWhenResume(
            this,
            ClientUpdateManager.appUpdateManager)
    }
}
