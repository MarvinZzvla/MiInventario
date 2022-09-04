package com.vendetta.miinventario.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vendetta.miinventario.R
import com.vendetta.miinventario.ventasProviderList

class VentasAdapter(val ventasList:ArrayList<Ventas>,val database:String) : RecyclerView.Adapter<VentasViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VentasViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return VentasViewHolder(layoutInflater.inflate(R.layout.card_layout,parent,false))
    }

    override fun onBindViewHolder(holder: VentasViewHolder, position: Int) {
        var item = ventasList[position]
        holder.render(item,database)
    }

    override fun getItemCount(): Int {
        return ventasList.size
    }
}