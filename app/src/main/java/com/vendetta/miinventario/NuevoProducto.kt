package com.vendetta.miinventario

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.vendetta.miinventario.recycler.Productos
import kotlinx.android.synthetic.main.activity_nuevo_producto.*

private lateinit var auth: FirebaseAuth
private var database = ""
class NuevoProducto : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_producto)
        auth = Firebase.auth
        loadPreferences()

        crearNuevoProducto_btn.setOnClickListener {
            Firebase.database.getReference(database).child("Productos").child(nuevoNameProducto.text.toString()).setValue(Productos(
                nuevoNameProducto.text.toString(),nuevoPrecio_producto.text.toString(),
                nuevaCantidadProducto.text.toString()

            ))
            Intent(this,ProductosHome::class.java).apply { startActivity(this) }
        }
    }

    fun loadPreferences(){
        getSharedPreferences("login_prefs", Context.MODE_PRIVATE).apply {
            database = this.getString("database","null").toString()
        }
    }
}