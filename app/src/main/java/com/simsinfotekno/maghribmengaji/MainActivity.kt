package com.simsinfotekno.maghribmengaji

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.simsinfotekno.maghribmengaji.databinding.ActivityMainBinding
import com.simsinfotekno.maghribmengaji.model.MaghribMengajiUser

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        val testUser = MaghribMengajiUser("Damar Maulana", "ibn.damr@gmail.com", 10)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Test user setup
        testUser.finishedPageIds = listOf(0,1,2,3,4)
        testUser.achievementIds = listOf()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

    }
}