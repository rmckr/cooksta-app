package com.example.cooksta_app

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.cooksta_app.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false

        setContentView(binding.root)

        val bottomNav: BottomNavigationView = binding.navView
        val navHostFragment: NavHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        bottomNav.setupWithNavController(navController)

        fun navigateToTab(destinationId: Int) {
            navController.popBackStack(navController.graph.startDestinationId, false)
            navController.navigate(destinationId)
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navigateToTab(R.id.navigation_home)
                    true
                }
                R.id.navigation_search -> {
                    navigateToTab(R.id.navigation_search)
                    true
                }
                R.id.navigation_add -> {
                    navigateToTab(R.id.navigation_add)
                    true
                }
                R.id.navigation_shopping_list -> {
                    navigateToTab(R.id.navigation_shopping_list)
                    true
                }
                R.id.navigation_profile -> {
                    navigateToTab(R.id.navigation_profile)
                    true
                }
                else -> false
            }
        }

        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (!isLoggedIn) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}