package com.vendetta.miinventario.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vendetta.miinventario.R
import com.vendetta.miinventario.data.Ventas

class VentasAdapter(private val ventasList: List<Ventas>) :
    RecyclerView.Adapter<VentasViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VentasViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return VentasViewHolder(layoutInflater.inflate(R.layout.item_ventas, parent, false))
    }

    override fun getItemCount(): Int {
        return ventasList.size
    }

    override fun onBindViewHolder(holder: VentasViewHolder, position: Int) {
        val item = ventasList[position]
        holder.render(item)
    }
}