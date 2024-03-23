package com.vendetta.miinventario.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.vendetta.miinventario.R
import com.vendetta.miinventario.data.Productos

class ProductosAdapter(private val productoList : List<Productos>,var onItemClickedProducto:(Productos)-> Unit)
    : RecyclerView.Adapter<ProductosViewHolder>(),Filterable {
    var listaFiltrada = productoList
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductosViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ProductosViewHolder(layoutInflater.inflate(R.layout.item_productos,parent,false))
    }

    override fun getItemCount(): Int {
    return listaFiltrada.size
    }


    override fun onBindViewHolder(holder: ProductosViewHolder, position: Int) {
        val item =listaFiltrada[position]
        holder.render(item,onItemClickedProducto)
    }
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint.toString()
                listaFiltrada = if (charString.isEmpty()) {
                    productoList
                } else {
                    val filteredList = ArrayList<Productos>()
                    for (producto in productoList) {
                        // Aquí pones la condición para filtrar tus elementos. Por ejemplo, si quieres filtrar por nombre:
                        if (producto.productoName.toLowerCase().contains(charString.toLowerCase()) ||producto.barCode.toString().startsWith(charString) ) {
                            filteredList.add(producto)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = listaFiltrada
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                listaFiltrada = results?.values as List<Productos>
                notifyDataSetChanged()
            }
        }
    }

}