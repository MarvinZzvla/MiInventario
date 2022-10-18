package com.vendetta.miinventario

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.vendetta.miinventario.recycler.Productos
import kotlinx.android.synthetic.main.activity_nuevo_producto.*

private lateinit var auth: FirebaseAuth
private val fireData = Firebase.firestore
private var database = ""
class NuevoProducto : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_producto)
        auth = Firebase.auth
        loadPreferences()
        banner_nuevoProducto.loadAd(AdRequest.Builder().build())

        crearNuevoProducto_btn.setOnClickListener {
            if (checkFields()){
                createProducto()
            }else{
                Toast.makeText(this,"Completa porfavor todos los campos",Toast.LENGTH_SHORT).show()
            }

        }
    }

    fun createProducto(){
        fireData.collection("db1").document(database).collection("Productos").document(nuevoNameProducto.text.toString()).set(Productos(
            nuevoNameProducto.text.toString(),nuevoPrecio_producto.text.toString(),
            nuevaCantidadProducto.text.toString()
        ))
        Intent(this,ProductosHome::class.java).apply { startActivity(this) }
    }

    fun checkFields():Boolean{
        return nuevoNameProducto.text.isNotEmpty() && nuevaCantidadProducto.text.isNotEmpty() && nuevoPrecio_producto.text.isNotEmpty()
    }

    fun loadPreferences(){
        getSharedPreferences("login_prefs", Context.MODE_PRIVATE).apply {
            database = this.getString("database","null").toString()
        }
    }
}