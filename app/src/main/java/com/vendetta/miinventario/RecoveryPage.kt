package com.vendetta.miinventario

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.vendetta.miinventario.data.database.InventarioDatabase
import com.vendetta.miinventario.data.database.InventarioDatabase.Companion.getDatabase
import com.vendetta.miinventario.databinding.ActivityRecoveryPageBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecoveryPage : AppCompatActivity() {
    lateinit var binding: ActivityRecoveryPageBinding
    lateinit var database: InventarioDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecoveryPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = getDatabase(this)

        binding.btnRecovery.setOnClickListener {
            var text = binding.editTextRecovery.text.toString()
            lifecycleScope.launch((Dispatchers.IO)) {
                var user = database.userDao.getAllUser()[0]

                withContext(Dispatchers.Main) {
                    if (user.user.toString() == text || user.pin.toString() == text || user.telefono.toString() == text) {
                        println("Usuario encontrado")
                        val textInfo = binding.displayInfdRecovery
                        textInfo.visibility = View.VISIBLE
                        textInfo.text = "Usuario: ${user.user}\nPIN: ${user.pin}\nTelefono: ${user.telefono}"
                    }
                }
            }

        }
    }

    override fun onStart() {
        super.onStart()
        binding.displayInfdRecovery.visibility = View.GONE
    }
}