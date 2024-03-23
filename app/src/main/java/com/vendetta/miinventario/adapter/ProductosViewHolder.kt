package com.vendetta.miinventario.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.vendetta.miinventario.data.Productos
import com.vendetta.miinventario.databinding.ItemProductosBinding

class ProductosViewHolder(view : View) : ViewHolder(view) {
    val binding = ItemProductosBinding.bind(view)
    val producto_name = binding.productoNameText
    val cantidad = binding.productoCantidadText
    val precio = binding.productoPrecioText
    val ganancia = binding.productoGananciaText
    fun render(producto: Productos, onItemClickedProducto: (Productos) -> Unit){

        producto_name.text = producto.productoName
        cantidad.text ="Disponibles: ${producto.cantidad.toString()}"
        precio.text = "Precio: $${producto.precio_venta.toString()}"
        ganancia.text = "Ganancias: $${(producto.precio_venta - producto.precio_costo).toString()}"
        binding.root.setOnClickListener {
            println(producto.barCode)
            onItemClickedProducto(producto)
        }

    }
}