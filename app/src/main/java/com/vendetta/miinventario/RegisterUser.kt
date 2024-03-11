package com.vendetta.miinventario

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.vendetta.miinventario.data.database.InventarioDatabase
import com.vendetta.miinventario.data.database.InventarioDatabase.Companion.getDatabase
import com.vendetta.miinventario.data.database.entities.UserEntity
import com.vendetta.miinventario.databinding.ActivityRegisterUserBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterUser : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterUserBinding
    private lateinit var database: InventarioDatabase
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Initialize Database
        database = getDatabase(this)

        //Cuando el usuario le de click a registrar
        binding.btnLogin.setOnClickListener {
        val userName =  binding.userText.text
        val pin = binding.newPinText.text
            if(checkFields(userName,pin)){
                println("VALIDO")
                saveUser(userName.toString(),pin.toString())
            }
            else{
                Toast.makeText(this,"Llene todos los campos",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUser(userName: String, pin: String) {
        lifecycleScope.launch(Dispatchers.IO) {
        database.userDao.insertAll(UserEntity(id=1,user = userName,pin = pin))
            withContext(Dispatchers.Main){
        Toast.makeText(applicationContext,"Usuario registrado",Toast.LENGTH_SHORT).show()
        Intent(applicationContext,MainActivity::class.java).apply { startActivity(this) }
        }

        }
    }

    private fun checkFields(userName: Editable, pin: Editable):Boolean{
        return userName.isNotEmpty() && pin.isNotEmpty()
    }
}