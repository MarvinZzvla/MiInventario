package com.vendetta.miinventario.data

import android.content.Context
import com.vendetta.miinventario.data.database.InventarioDatabase
import com.vendetta.miinventario.data.database.InventarioDatabase.Companion.getDatabase

class VentaDropdownProvider {
    private lateinit var database:InventarioDatabase

    suspend fun getProductos(context: Context): ArrayList<VentaDropdown> {
        database = getDatabase(context)
        val productosList = database.productosDao.getAllProductos()
        var dropdownList = listOf<VentaDropdown>()
        for (producto in productosList){
            dropdownList += VentaDropdown(producto.ID,producto.Name,producto.Available.toString(),producto.Price,producto.Price_Sell,producto.BarCode)
        }

//        val dropdownList = listOf<VentaDropdown>(
//            VentaDropdown("Camiseta","10"),
//            VentaDropdown("Pantalon","5"),
//            VentaDropdown("Licra","10"),
//            VentaDropdown("Short","50"),
//            VentaDropdown("Camisola","5"),
//            VentaDropdown("Sudadera","80")
//        )
        val mutableList = ArrayList(dropdownList)
        return mutableList
    }

}