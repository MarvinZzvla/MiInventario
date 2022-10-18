package com.vendetta.miinventario.recycler

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.vendetta.miinventario.EditarProducto
import com.vendetta.miinventario.R

class ProductosViewHolder (view: View): RecyclerView.ViewHolder(view) {
    private val fireData = Firebase.firestore

    var productoName = view.findViewById<TextView>(R.id.producto_item_title)
    var productoPrecio = view.findViewById<TextView>(R.id.producto_item_precio)
    var productoCantidad = view.findViewById<TextView>(R.id.producto_item_cantidad)
    var deleteProducto = view.findViewById<ImageButton>(R.id.deleteProductoBtn)
    var editProducto =  view.findViewById<ImageButton>(R.id.editarProductoBtn)

    fun render(productos: Productos, database: String){
        productoName.text = productos.name
        productoPrecio.text = productos.precio
        productoCantidad.text = productos.cantidad

        deleteProducto.setOnClickListener{
            showAlertDialog(productos, database)

        }
        editProducto.setOnClickListener {
            editProductoScreen(productoName,productos)
        }
    }

    fun showAlertDialog(productos: Productos, database: String) {
        val alertDialog: AlertDialog? = productoName.context?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton("Si",
                    DialogInterface.OnClickListener { dialog, id ->
                        fireData.collection("db1").document(database).collection("Productos").document(productos.name).delete().addOnSuccessListener {
                            Toast.makeText(builder.context,"Eliminado con exito", Toast.LENGTH_SHORT).show()
                        }
                        //Firebase.database.getReference(database).child("Productos").child(productos.name).removeValue()
                    })
                setNegativeButton("No",
                    DialogInterface.OnClickListener { dialog, id ->
                        // User cancelled the dialog
                    })
                setTitle("Eliminar registro")
                setMessage("¿Desea realmente eliminar este registro?")
                show()
            }
            builder.create()
        }
    }

    fun editProductoScreen(view: View, productos: Productos) {
        Intent(view.context, EditarProducto::class.java).apply {
            this.putExtra("productoName",productos.name)
            //this.putExtra("productoCantidad",productos.cantidad)
            //this.putExtra("productoPrecio",productos.precio)
            startActivity(view.context,this,null)
        }
    }
}