package com.simsinfotekno.maghribmengaji

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.simsinfotekno.maghribmengaji.databinding.ActivityLoginBinding
import com.simsinfotekno.maghribmengaji.enums.ConnectivityObserver
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LoginActivity : AppCompatActivity() {

    companion object {
        private val TAG = LoginActivity::class.java.simpleName
    }

    //    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityLoginBinding

    /* Variables */
    private lateinit var auth: FirebaseAuth
    private lateinit var connectivityObserver: ConnectivityObserver
    private var connectionStatus: ConnectivityObserver.Status =
        ConnectivityObserver.Status.Unavailable

    private lateinit var navController: NavController

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        connectivityObserver = NetworkConnectivityObserver(applicationContext)
        connectivityObserver.observe().onEach {
            connectionStatus = it
            Log.d(TAG, "status is $connectionStatus")
        }.launchIn(lifecycleScope)

        /* Views */
        navController =
            (supportFragmentManager.findFragmentById(binding.loginContent.navHostFragmentContentLogin.id) as NavHostFragment).navController

        // Set the status bar color
        window.statusBarColor =
            ContextCompat.getColor(this, R.color.md_theme_primaryContainer)
        setStatusBarTextColor(isLightTheme = false)// Set the status bar text color

        // Bottom sheet attributes
        bottomSheetBehavior =
            BottomSheetBehavior.from(binding.loginContent.loginBottomSheet)

//        setSupportActionBar(binding.toolbar)

//        val navController = findNavController(R.id.nav_host_fragment_content_login)
//        appBarConfiguration = AppBarConfiguration(navController.graph)
//        setupActionBarWithNavController(navController, appBarConfiguration)

        /* Auth */
        auth = Firebase.auth // Initialize Firebase Auth
        val currentUser = auth.currentUser // Get current user

        if (currentUser != null) {
            Log.d(TAG, "Logged in. Navigating to user dashboard.")

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            this.finish()  // Finish the current activity so the user can't navigate back to the login screen
        }

        /* Listeners */
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.welcomeFragment -> {
                    Handler(Looper.getMainLooper()).postDelayed({
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    }, 250)
                }

                else -> {
                    Handler(Looper.getMainLooper()).postDelayed({
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }, 250)
                }
            }
        }

    }

    private fun setStatusBarTextColor(isLightTheme: Boolean) {
        val decor = window.decorView
        if (isLightTheme) {
            decor.systemUiVisibility =
                decor.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            decor.systemUiVisibility =
                decor.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }

//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment_content_login)
//        return navController.navigateUp(appBarConfiguration)
//                || super.onSupportNavigateUp()
//    }
}