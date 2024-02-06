package com.vendetta.miinventario.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vendetta.miinventario.R
import com.vendetta.miinventario.data.Factura

class FacturaAdapter(private val facturaList: List<Factura>) :RecyclerView.Adapter<FacturaViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacturaViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return FacturaViewHolder(layoutInflater.inflate(R.layout.factura_recycle_layout,parent,false))
    }

    override fun getItemCount(): Int {
        return facturaList.size
    }

    override fun onBindViewHolder(holder: FacturaViewHolder, position: Int) {
        val item = facturaList[position]
        holder.render(item)
    }
}