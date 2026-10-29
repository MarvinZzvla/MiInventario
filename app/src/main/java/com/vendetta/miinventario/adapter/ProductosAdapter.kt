package com.vendetta.miinventario.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.vendetta.miinventario.R
import com.vendetta.miinventario.data.Productos

class ProductosAdapter(
    private val productoList: List<Productos>,
    var onItemClickedProducto: (Productos) -> Unit
) : RecyclerView.Adapter<ProductosViewHolder>(), Filterable {

    private var listaFiltrada: List<Productos> = productoList.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductosViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ProductosViewHolder(layoutInflater.inflate(R.layout.item_productos, parent, false))
    }

    override fun getItemCount(): Int = listaFiltrada.size

    override fun onBindViewHolder(holder: ProductosViewHolder, position: Int) {
        val item = listaFiltrada[position]
        holder.render(item, onItemClickedProducto)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString().orEmpty()
                val filtered = if (charString.isEmpty()) {
                    productoList
                } else {
                    productoList.filter { producto ->
                        producto.productoName.lowercase().contains(charString.lowercase()) ||
                            producto.barCode.toString().startsWith(charString)
                    }
                }
                return FilterResults().apply {
                    values = filtered
                    count = filtered.size
                }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                listaFiltrada = (results?.values as? List<Productos>) ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }
}
