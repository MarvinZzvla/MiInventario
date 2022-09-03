package com.vendetta.miinventario.recycler

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vendetta.miinventario.R

class ProductosViewHolder (view: View): RecyclerView.ViewHolder(view) {

    var productoName = view.findViewById<TextView>(R.id.producto_item_title)
    var productoPrecio = view.findViewById<TextView>(R.id.producto_item_precio)
    var productoCantidad = view.findViewById<TextView>(R.id.producto_item_cantidad)
    fun render(productos: Productos){
        productoName.text = productos.name
        productoPrecio.text = productos.precio
        productoCantidad.text = productos.cantidad
    }
}