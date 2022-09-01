package com.vendetta.miinventario.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vendetta.miinventario.R
import com.vendetta.miinventario.ventasProviderList

class VentasAdapter(val ventasList:ArrayList<Ventas>) : RecyclerView.Adapter<VentasViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VentasViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return VentasViewHolder(layoutInflater.inflate(R.layout.card_layout,parent,false))
    }

    override fun onBindViewHolder(holder: VentasViewHolder, position: Int) {
        var item = ventasProviderList[position]
        holder.render(item)
    }

    override fun getItemCount(): Int {
        return ventasList.size
    }
}