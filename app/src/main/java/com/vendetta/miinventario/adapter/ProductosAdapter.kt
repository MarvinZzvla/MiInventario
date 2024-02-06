package com.vendetta.miinventario.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vendetta.miinventario.R
import com.vendetta.miinventario.data.Productos
import com.vendetta.miinventario.data.Ventas

class ProductosAdapter(private val productoList : List<Productos>) : RecyclerView.Adapter<ProductosViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductosViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ProductosViewHolder(layoutInflater.inflate(R.layout.item_productos,parent,false))
    }

    override fun getItemCount(): Int {
    return productoList.size
    }

    override fun onBindViewHolder(holder: ProductosViewHolder, position: Int) {
    val item =productoList[position]
        holder.render(item)
    }
}