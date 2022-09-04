package com.vendetta.miinventario.recycler

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.vendetta.miinventario.R

class ProductosViewHolder (view: View): RecyclerView.ViewHolder(view) {

    var productoName = view.findViewById<TextView>(R.id.producto_item_title)
    var productoPrecio = view.findViewById<TextView>(R.id.producto_item_precio)
    var productoCantidad = view.findViewById<TextView>(R.id.producto_item_cantidad)
    var deleteProducto = view.findViewById<ImageButton>(R.id.deleteProductoBtn)

    fun render(productos: Productos, database: String){
        productoName.text = productos.name
        productoPrecio.text = productos.precio
        productoCantidad.text = productos.cantidad
        deleteProducto.setOnClickListener{
            Firebase.database.getReference(database).child("Productos").child(productos.name).removeValue()
        }
    }
}