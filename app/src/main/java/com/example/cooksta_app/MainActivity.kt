package com.example.cooksta_app

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.cooksta_app.databinding.ActivityMainBinding
import com.example.cooksta_app.network.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val displayMetrics = resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density

        if (screenWidthDp < 600) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        binding = ActivityMainBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

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

        RetrofitClient.initialize(applicationContext)

        if (!RetrofitClient.isLoggedIn) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}