package com.vendetta.miinventario

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_editar_producto.*

class EditarProducto : AppCompatActivity() {
    var database = ""
    val fireData = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_producto)
        loadPreferences()
        initVariables()

        editarNuevoProducto_btn.setOnClickListener{
            updateProducto()
        }
    }

    fun updateProducto(){
        var nameProducto = editarNameProducto.text.toString()
        var precio = editarPrecio_producto.text.toString()
        var cantidad = editarCantidadProducto.text.toString()

        /*
        Firebase.database.getReference(database).child("Productos").child(nameProducto).child("precio").setValue(precio)
        Firebase.database.getReference(database).child("Productos").child(nameProducto).child("cantidad").setValue(cantidad)
        */

        fireData.collection("db1").document(database).collection("Productos").document(nameProducto).update("precio",precio)
        fireData.collection("db1").document(database).collection("Productos").document(nameProducto).update("cantidad",cantidad)

        Toast.makeText(this,"Editado con exito",Toast.LENGTH_SHORT).show()
        Intent(this,ProductosHome::class.java).apply { startActivity(this) }
    }

    fun initVariables(){
        //editarNameProducto.isEnabled = false;
        editarNameProducto.isFocusable = false;
        editarNameProducto.isActivated = false;
        editarNameProducto.setOnClickListener {
            Toast.makeText(this,"No es posible modificar nombre del registro",Toast.LENGTH_SHORT).show()

        }

        var nameProducto = intent.getStringExtra("productoName")
        editarNameProducto.text = Editable.Factory.getInstance().newEditable(nameProducto)

        /*if (nameProducto != null) {
            Firebase.database.getReference(database).child("Productos").child(nameProducto).get().addOnSuccessListener {
                editarPrecio_producto.text = Editable.Factory.getInstance().newEditable(it.child("precio").value.toString())
                editarCantidadProducto.text = Editable.Factory.getInstance().newEditable(it.child("cantidad").value.toString())
            }
        }*/
        if (nameProducto != null) {
            fireData.collection("db1").document(database).collection("Productos")
                .document(nameProducto).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    println("Esta es: " + snapshot.data?.get("precio"))
                    editarPrecio_producto.text = Editable.Factory.getInstance()
                        .newEditable(snapshot.data?.get("precio").toString())
                    editarCantidadProducto.text = Editable.Factory.getInstance()
                        .newEditable(snapshot.data?.get("cantidad").toString())
                }
            }
        }

    }

    fun loadPreferences(){
        getSharedPreferences("login_prefs", Context.MODE_PRIVATE).apply {
            database = this.getString("database","null").toString()
        }

        /*
        getSharedPreferences("login_prefs", Context.MODE_PRIVATE).apply {
            isAdmin = this.getBoolean("isAdmin",false)
        }

        getSharedPreferences("login_prefs", Context.MODE_PRIVATE).apply {
            fullname = this.getString("name","null").toString()
        }*/
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Intent(this,ProductosHome::class.java).apply { startActivity(this) }
    }
}