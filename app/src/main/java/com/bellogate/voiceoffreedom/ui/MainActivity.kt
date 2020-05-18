package com.bellogate.voiceoffreedom.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bellogate.voiceoffreedom.R
import com.bellogate.voiceoffreedom.data.showSnackMessage
import com.bellogate.voiceoffreedom.data.showSnackMessageAtTop
import com.bellogate.voiceoffreedom.model.User
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

const val  RC_SIGN_IN = 44

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var viewModel: SharedViewModel
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_home,
            R.id.nav_gallery,
            R.id.nav_slideshow
        ), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        toolbar.setTitleTextColor(resources.getColor(R.color.black))
        toolbar.setTitleTextAppearance(this,
            R.style.LatoBoldTextAppearance
        )//change the font

        viewModel = ViewModelProviders.of(this).get(SharedViewModel::class.java)

        //we place a constant listener to know when the user has signed out,
        // So that we can know when to delete the user from db
        //This will trigger anytime the user sign out. Successfully or not.
        viewModel.listenForUserSignOut(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
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
            16908332 ->{
                drawerLayout.openDrawer(Gravity.LEFT)
            }
        }
        return true
    }

    private fun logout() = viewModel.logout(this)


    /**
     * Uses firebase UI auth to sign up a user
     * ***/
    private fun launchFirebaseAuthentication() = startActivityForResult(AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(viewModel.getAuthProviders())
        .build(),
        RC_SIGN_IN
    )


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            handleSignIn(resultCode, data)
        }
    }


    private fun handleSignIn(resultCode : Int, data: Intent?) {
        val response = IdpResponse.fromResultIntent(data)
        val view = this.window?.decorView?.rootView

        if (resultCode == Activity.RESULT_OK) {
            // Successfully signed in
            val fireBaseUser = FirebaseAuth.getInstance().currentUser
            val user = User(1,fireBaseUser?.displayName ?: "You", fireBaseUser!!.email!!,
                System.currentTimeMillis(), false)
            //finally save the user:
            viewModel.saveUser(this, lifecycleScope, 1, user)
            showSnackMessageAtTop(this, view!!, "Login successful!!")
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
            showSnackMessage(view!!, "Cancelled")
        }else{
            showSnackMessage(view!!, ErrorCodes.toFriendlyMessage(response.error!!.errorCode))
        }
    }

}