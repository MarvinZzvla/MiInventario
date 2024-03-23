package com.vendetta.miinventario.data

import android.content.Context
import com.vendetta.miinventario.data.database.InventarioDatabase
import com.vendetta.miinventario.data.database.InventarioDatabase.Companion.getDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductosProvider {
    lateinit var database: InventarioDatabase
    suspend fun getUser(context: Context): List<Productos> {
        database = getDatabase(context)

        val productosDatabaseList = database.productosDao.getAllProductos()
        var productoList = listOf<Productos>()
        for (producto in productosDatabaseList){
            productoList += Productos(producto.ID,producto.Name,producto.Available,producto.Price,producto.Price_Sell,producto.BarCode)
        }

        return productoList
    }

}