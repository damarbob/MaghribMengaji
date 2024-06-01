package com.simsinfotekno.maghribmengaji

import android.content.pm.PackageManager
import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.simsinfotekno.maghribmengaji.databinding.ActivityRecordingBinding
import com.simsinfotekno.maghribmengaji.ui.recording.RecordingFragment

class RecordingActivity : AppCompatActivity() {

    companion object{
        private val TAG = RecordingActivity::class.java.simpleName
    }

    private lateinit var binding: ActivityRecordingBinding

    private lateinit var navController: NavController

    private var pageId: Int = -1

    private val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_MEDIA_AUDIO)
    } else {
        arrayOf(Manifest.permission.RECORD_AUDIO)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var allGranted = true
        for ((_, granted) in permissions) {
            if (!granted) {
                allGranted = false
                break
            }
        }
        if (allGranted) {
            Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRecordingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pageId = intent.getIntExtra("pageId", -1)
        Log.d(TAG, pageId.toString())

        val bundle = Bundle().apply {
            putInt("pageId", pageId)
        }
        navController = (supportFragmentManager.findFragmentById(binding.navHostFragmentActivityRecording.id) as NavHostFragment).navController
        navController.setGraph(R.navigation.nav_recording, bundle)

//        if (savedInstanceState == null) {
//            replaceFragment(RecordingFragment())
//        }

        requestPermission()

//        setSupportActionBar(binding.recordingActivityToolbar)

//        binding.fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null)
//                .setAnchorView(R.id.fab).show()
//        }
    }

//    fun replaceFragment(fragment: Fragment) {
//        val bundle = Bundle().apply {
//            putInt("pageId", pageId)
//        }
//        supportFragmentManager
//            .beginTransaction()
//            .replace(R.id.recordingActivityFrameLayoutContainer, fragment.javaClass, bundle)
//            .commit()
//        supportFragmentManager.popBackStack()
//    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val result1 = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        when {
            permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED } -> {
                // All permissions are granted
            }
            else -> {
                requestPermissionLauncher.launch(permissions)
            }
        }
    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            1 -> if (grantResults.isNotEmpty()) {
//                val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
//                val recordAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
//                if (storageAccepted && recordAccepted) {
//                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO), 1)
//                        }
//                    }
//                }
//            }
//        }
//    }
}