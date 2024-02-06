package com.vendetta.miinventario.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.vendetta.miinventario.data.Factura
import com.vendetta.miinventario.databinding.FacturaRecycleLayoutBinding

class FacturaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val binding = FacturaRecycleLayoutBinding.bind(view)
    val item = binding.facturaItem
    val cantidad = binding.facturaCantidad
    val precio = binding.facturaPrecio
    fun render(factura: Factura){
        item.text = factura.item
        cantidad.text = factura.cantidad.toString()
        precio.text = factura.precio.toString()
    }
}