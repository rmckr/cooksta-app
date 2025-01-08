package com.example.cooksta_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cooksta_app.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.saveProfile -> {
                    // Save profile
                    onBackPressedDispatcher.onBackPressed()
                    true
                }
                else -> false
            }
        }

        binding.saveProfileButton.setOnClickListener {
            // Save profile
            onBackPressedDispatcher.onBackPressed()
        }
    }
}