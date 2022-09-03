package com.vendetta.miinventario.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vendetta.miinventario.R
import com.vendetta.miinventario.productosProviderList

class ProductosAdapter (val productosList:ArrayList<Productos>) : RecyclerView.Adapter<ProductosViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductosViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ProductosViewHolder(layoutInflater.inflate(R.layout.producto_card_layout,parent,false))
    }

    override fun onBindViewHolder(holder: ProductosViewHolder, position: Int) {
        var item = productosProviderList[position]
        holder.render(item)
    }

    override fun getItemCount(): Int {
        return productosList.size
    }
}