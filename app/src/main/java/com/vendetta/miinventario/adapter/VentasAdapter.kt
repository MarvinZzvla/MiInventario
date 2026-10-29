package com.vendetta.miinventario.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.vendetta.miinventario.R
import com.vendetta.miinventario.data.Ventas

class VentasAdapter(
    private val ventasList: List<Ventas>,
    private val onItemClicked: (Ventas) -> Unit
) : RecyclerView.Adapter<VentasViewHolder>(), Filterable {

    private var listaFiltrada: List<Ventas> = ventasList.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VentasViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return VentasViewHolder(layoutInflater.inflate(R.layout.item_ventas, parent, false))
    }

    override fun getItemCount(): Int = listaFiltrada.size

    override fun onBindViewHolder(holder: VentasViewHolder, position: Int) {
        val item = listaFiltrada[position]
        holder.render(item, onItemClicked)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString().orEmpty()
                val filtered = if (charString.isEmpty()) {
                    ventasList
                } else {
                    ventasList.filter { venta ->
                        venta.factura.toString().contains(charString)
                    }
                }
                return FilterResults().apply {
                    values = filtered
                    count = filtered.size
                }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                listaFiltrada = (results?.values as? List<Ventas>) ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }
}
