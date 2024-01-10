package com.vendetta.miinventario


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.vendetta.miinventario.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //JUST FOR TESTING PURPOSE
        Intent(this,HomePage::class.java).apply { startActivity(this) }

        binding.btnLogin.setOnClickListener {
            Intent(this,HomePage::class.java).apply { startActivity(this) }
            binding.editTextText.text.clear()
            binding.editPin.text.clear()
        }
    }

    }



