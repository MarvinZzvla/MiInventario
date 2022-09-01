package com.vendetta.miinventario.recycler

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vendetta.miinventario.R

class VentasViewHolder(view: View):RecyclerView.ViewHolder(view) {

    var ventaName = view.findViewById<TextView>(R.id.item_title)
    var ventaPrecio = view.findViewById<TextView>(R.id.item_precio)
    var ventasDate = view.findViewById<TextView>(R.id.item_date)
    fun render(ventas:Ventas){
        ventaName.text = ventas.venta_name
        ventaPrecio.text = ventas.venta_precio
        ventasDate.text = ventas.venta_date
    }
}