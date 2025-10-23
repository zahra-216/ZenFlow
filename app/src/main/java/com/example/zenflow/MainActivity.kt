package com.example.zenflow

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.zenflow.data.HabitSharedPreferencesManager
import com.example.zenflow.data.ProfileSharedPreferencesManager
import com.example.zenflow.databinding.ActivityMainBinding
import com.example.zenflow.util.HabitHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if user is logged in
        val profilePrefs = ProfileSharedPreferencesManager(this)
        if (!profilePrefs.isLoggedIn()) {
            // User not logged in, redirect to SigninActivity
            val intent = Intent(this, SigninActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        // Reset daily completion if it's a new day
        resetDailyHabits()

        val navView: BottomNavigationView = binding.navView

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment

        val navController = navHostFragment.navController

        navView.setupWithNavController(navController)
    }

    private fun resetDailyHabits() {
        val habitPrefs = HabitSharedPreferencesManager(this)
        val habits = habitPrefs.loadHabits()
        val resetHabits = HabitHelper.resetDailyCompletion(habits)
        habitPrefs.saveHabits(resetHabits)
    }

    fun navigateToHabitsTab() {
        binding.navView.selectedItemId = R.id.navigation_habits
    }
}