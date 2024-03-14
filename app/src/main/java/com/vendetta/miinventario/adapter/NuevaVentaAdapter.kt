package com.vendetta.miinventario.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vendetta.miinventario.R
import com.vendetta.miinventario.data.structures.NuevaVentaDatos

class NuevaVentaAdapter(private val productoList : List<NuevaVentaDatos>): RecyclerView.Adapter<NuevaVentaViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NuevaVentaViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return NuevaVentaViewHolder(layoutInflater.inflate(R.layout.item_nueva_venta,parent,false))
    }

    override fun getItemCount(): Int {
        return productoList.size
    }

    override fun onBindViewHolder(holder: NuevaVentaViewHolder, position: Int) {
        val item = productoList[position]
        holder.render(item)
    }
}