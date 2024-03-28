package com.vendetta.miinventario

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.vendetta.miinventario.data.database.InventarioDatabase
import com.vendetta.miinventario.data.database.InventarioDatabase.Companion.getDatabase
import com.vendetta.miinventario.data.database.entities.UserEntity
import com.vendetta.miinventario.databinding.ActivityRegisterUserBinding
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.util.Locale

class RegisterUser : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterUserBinding
    private lateinit var database: InventarioDatabase
    private lateinit var supabase: SupabaseClient
    val paisesDeHablaHispana = listOf(
        "", "Argentina", "Andorra", "Bolivia", "Chile", "Colombia", "Costa Rica", "Cuba",
        "Ecuador", "El Salvador", "España", "Guatemala", "Guinea Ecuatorial",
        "Honduras", "México", "Nicaragua", "Panamá", "Paraguay", "Perú",
        "Puerto Rico", "República Dominicana", "Uruguay", "Venezuela"
    )
@Serializable
    data class UserEntitySupabase (
        val id: Int = 0,
        val user: String,
        val pin:String,
        val negocio:String,
        val phone:String,
        val pais:String
    )

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Initialize Database
        database = getDatabase(this)
        //Initialize supabase
        initSupabase()
        //Initialize Dropdown
        initSpinner()
        //Cuando el usuario le de click a registrar
        binding.btnLogin.setOnClickListener {
            it.isEnabled = false
            val userName = binding.userText.text
            val pin = binding.newPinText.text
            val negocio = binding.newNegocioText.text
            val phone = binding.newNumeroText.text
            var pais = binding.paisDropdown.selectedItem.toString()
            if (checkFields(userName, pin,negocio,phone,pais)) {
                saveUser(userName.toString(), pin.toString(), negocio.toString(), phone.toString(), pais)
            } else {
                it.isEnabled = true
                Toast.makeText(this, "Llene todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initSupabase() {
            supabase = createSupabaseClient(
            supabaseUrl = "https://simoaelxxamqdllinrir.supabase.co",
            supabaseKey = BuildConfig.SUPABASE_API_KEY
        ) {
            install(Postgrest)
        }
    }

    private fun initSpinner() {
        val spinner = binding.paisDropdown
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, paisesDeHablaHispana)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun saveUser(
        userName: String,
        pin: String,
        negocio: String,
        phone: String,
        pais: String
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
           database.userDao.insertAll(
                UserEntity(
                    id = 1, user = userName, pin = pin, negocio = negocio.capitalizeFirstLetter(),
                    telefono = phone, pais = pais
                )
            )
            val user =  UserEntitySupabase(user = userName, pin = pin, negocio = negocio.capitalizeFirstLetter(),phone = phone, pais = pais)
            try {
                supabase.from("Usuarios").insert(user)
            }
            catch (e:Exception){
                println(e.message)
            }
            val sharedPreferences = getSharedPreferences("login_users", Context.MODE_PRIVATE).edit()
            sharedPreferences.putBoolean("isRegister", true)
            sharedPreferences.apply()

            withContext(Dispatchers.Main) {
                Toast.makeText(applicationContext, "Usuario registrado", Toast.LENGTH_SHORT).show()
                Intent(applicationContext, MainActivity::class.java).apply { startActivity(this) }
            }

        }
    }

    private fun checkFields(
        userName: Editable,
        pin: Editable,
        negocio: Editable,
        phone: Editable,
        pais: String,): Boolean {
        return userName.isNotEmpty() && pin.isNotEmpty() && negocio.isNotEmpty() && phone.isNotEmpty()&&pais.isNotEmpty()
    }
    fun String.capitalizeFirstLetter(): String {
        return this.substring(0, 1).uppercase(Locale.ROOT) + this.substring(1)
    }
}

