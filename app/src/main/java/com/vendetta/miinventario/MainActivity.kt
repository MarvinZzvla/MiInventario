package com.vendetta.miinventario


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.vendetta.miinventario.data.database.InventarioDatabase
import com.vendetta.miinventario.data.database.InventarioDatabase.Companion.getDatabase
import com.vendetta.miinventario.data.database.entities.UserEntity
import com.vendetta.miinventario.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: InventarioDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Cargar sesion activa
        loadPrefs()

        binding.btnRegister.setOnClickListener {
            Intent(this, RegisterUser::class.java).apply { startActivity(this) }
        }

        database = getDatabase(this)

        //saveUser()
        binding.btnLogin.setOnClickListener {

            lifecycleScope.launch(Dispatchers.IO) {

                var users = readDatabase()

                withContext(Dispatchers.Main) {

                    if (!users.isNullOrEmpty()) {
                        //Si existe usuarios en la base datos
                        readUsers(users)
                    } else {
                        //No existe usuarios en la base de datos
                        Toast.makeText(
                            applicationContext,
                            "No tienes ningun usuario registrado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
        }
    } //END ONCREATE

    private fun readUsers(users: List<UserEntity>) {
        val name = users[0].user ?: ""
        val pin = users[0].pin ?: ""


        if (users.isNotEmpty() && verifyUser(name, pin)) {

            val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE).edit()
            sharedPreferences.putString("username", name)
            sharedPreferences.putString("pin", pin)
            sharedPreferences.putBoolean("isLogin", true)
            sharedPreferences.apply()
            Intent(this, HomePage::class.java).apply { startActivity(this) }
            println("Success")
        } else {

            Toast.makeText(this, "El usuario o contraseña son incorrectos", Toast.LENGTH_SHORT)
                .show()
        }

    }

    private fun verifyUser(name: String, pin: String): Boolean {
        return name == binding.editTextText.text.toString() && pin == binding.editPin.text.toString()
    }

    private fun loadPrefs() {
        var isLogin = false
        val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        isLogin = sharedPreferences.getBoolean("isLogin", false)
        if (isLogin) {
            Intent(this, HomePage::class.java).apply { startActivity(this) }
        }
    }


    suspend fun readDatabase(): List<UserEntity> {
        val users = database.userDao.getAllUser()

        return users

    }

}



