package com.example.cooksta_app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cooksta_app.databinding.ActivityLoginBinding
import com.example.cooksta_app.network.RetrofitClient
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private fun handleLogin(username: String, password: String) {
        lifecycleScope.launch {
            val loginSuccessful = RetrofitClient.login(username, password)
            if (loginSuccessful) {
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                binding.editTextUsername.setText("")
                binding.editTextPassword.setText("")

                Toast.makeText(this@LoginActivity, "Falsche Zugangsdaten", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            val username = binding.editTextUsername.text.toString()
            val password = binding.editTextPassword.text.toString()
            handleLogin(username, password)
        }
    }
}