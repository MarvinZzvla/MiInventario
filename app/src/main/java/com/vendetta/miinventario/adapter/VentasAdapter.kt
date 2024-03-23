package com.vendetta.miinventario.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.vendetta.miinventario.R
import com.vendetta.miinventario.data.Ventas
import com.vendetta.miinventario.data.structures.NuevaVentaDatos

class VentasAdapter(private val ventasList: List<Ventas>,private val onItemClicked:(Ventas) -> Unit) :
    RecyclerView.Adapter<VentasViewHolder>(),Filterable {
    var listaFiltrada = ventasList
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VentasViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return VentasViewHolder(layoutInflater.inflate(R.layout.item_ventas, parent, false))
    }

    override fun getItemCount(): Int {
        return listaFiltrada.size
    }

    override fun onBindViewHolder(holder: VentasViewHolder, position: Int) {
        val item = listaFiltrada[position]
        holder.render(item, onItemClicked)
    }

    override fun getFilter(): Filter {
        return object :Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint.toString()
                listaFiltrada = if (charString.isEmpty()) {
                    ventasList
                } else {
                    val filteredList = ArrayList<Ventas>()
                    for (venta in ventasList) {
                        // Aquí pones la condición para filtrar tus elementos. Por ejemplo, si quieres filtrar por cantidad:
                        if (venta.factura.toString().contains(charString)) {
                            filteredList.add(venta)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = listaFiltrada
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                listaFiltrada = results?.values as List<Ventas>
                println("Esta es " + listaFiltrada)
                notifyDataSetChanged()
            }

        }
    }
}