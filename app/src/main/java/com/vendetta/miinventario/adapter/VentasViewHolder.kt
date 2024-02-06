package com.vendetta.miinventario.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.vendetta.miinventario.R
import com.vendetta.miinventario.data.Ventas
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
    fun render(venta: Ventas){
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        date.text = simpleDateFormat.format(venta.fecha)
        factura.text = "#${venta.factura.toString()}"
        ventas.text = venta.objetos.joinToString(separator = ", ","Items: " )
        totalVentas.text = "$${venta.totalVenta.toString()}"
        gananciaVentas.text = "$${venta.totalGanancia.toString()}"
    }
}