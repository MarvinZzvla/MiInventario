package com.vendetta.miinventario.data

import android.content.Context
import com.vendetta.miinventario.data.database.InventarioDatabase
import com.vendetta.miinventario.data.database.InventarioDatabase.Companion.getDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductosProvider() {
    lateinit var database: InventarioDatabase
    suspend fun getUser(context: Context): List<Productos> {
        database = getDatabase(context)


        val productosDatabaseList = database.productosDao.getAllProductos()
        var productoList = listOf<Productos>(


//            Productos(1, "Camiseta", 100, 60.0f, 120.0f, "0010405"),
//            Productos(2, "mINECRAFT", 50, 60.0f, 120.0f, "0010406"),
//            Productos(3, "Licra", 60, 40.0f, 60.0f, "0010407"),
//            Productos(4, "Short", 40, 40.0f, 70.0f, "0010408"),
//            Productos(5, "Sueter", 20, 10.0f, 20.0f, "0010409"),

            )
        for (producto in productosDatabaseList){
            productoList += Productos(producto.ID,producto.Name,producto.Available,producto.Price,producto.Price_Sell,"001")
        }







        return productoList
    }
//    companion object{
// val productoList = listOf<Productos>(
//
//     Productos(1,"Camiseta",100,60.0f,120.0f,"0010405"),
//     Productos(2,"Pantalon",50,60.0f,120.0f,"0010406"),
//     Productos(3,"Licra",60,40.0f,60.0f,"0010407"),
//     Productos(4,"Short",40,40.0f,70.0f,"0010408"),
//     Productos(5,"Sueter",20,10.0f,20.0f,"0010409"),
// )
//    }
}