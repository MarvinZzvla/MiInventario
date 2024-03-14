package com.vendetta.miinventario.adapter

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.vendetta.miinventario.data.structures.NuevaVentaDatos
import com.vendetta.miinventario.databinding.ItemNuevaVentaBinding

class NuevaVentaViewHolder(view: View) : ViewHolder(view) {
    val binding = ItemNuevaVentaBinding.bind(view)
    val name = binding.recycleNuevaVentaNameProducto
    val cantidad = binding.recycleNuevaVentaCantidadProducto
    val total = binding.recycleNuevaVentaTotalProducto
    @SuppressLint("SetTextI18n")
    fun render(item: NuevaVentaDatos) {
        name.text = "Nombre: ${item.name}"
        cantidad.text = "Cantidad: ${item.cantidad}"
        total.text = "Total: $${item.precio_total}"

    }
}