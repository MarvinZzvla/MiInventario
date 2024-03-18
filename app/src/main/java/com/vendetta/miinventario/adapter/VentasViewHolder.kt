package com.vendetta.miinventario.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.vendetta.miinventario.data.Ventas
import com.vendetta.miinventario.data.structures.NuevaVentaDatos
import com.vendetta.miinventario.databinding.ItemVentasBinding
import java.text.SimpleDateFormat
import java.util.Locale

class VentasViewHolder(view : View) : ViewHolder(view) {
    val binding = ItemVentasBinding.bind(view)
    val date = binding.dateText
    val factura = binding.facturaNumber
    val ventas = binding.listaVentas
    val totalVentas = binding.totalVentaText
    val gananciaVentas = binding.totalGananciaText

    fun render(venta: Ventas, onItemClicked: (Ventas) -> Unit){
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        date.text = simpleDateFormat.format(venta.fecha)
        //date.text = venta.fecha
        factura.text = "#${venta.factura.toString()}"
        ventas.text = venta.objetos.dropLast(1)
        totalVentas.text = "$${venta.totalVenta.toString()}"
        gananciaVentas.text = "$${venta.totalGanancia.toString()}"
        binding.root.setOnClickListener{onItemClicked(venta)}
    }
}