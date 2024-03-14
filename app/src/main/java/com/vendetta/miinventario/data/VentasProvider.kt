package com.vendetta.miinventario.data

import android.content.Context
import com.vendetta.miinventario.data.database.InventarioDatabase
import com.vendetta.miinventario.data.database.InventarioDatabase.Companion.getDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VentasProvider {
    lateinit var database : InventarioDatabase
    suspend fun getAllVentas(context: Context): List<Ventas>{
        database = getDatabase(context)
        val ventasDatabaseList = database.ventasDao.getAllVentas()
        var arrayIdFacturaDuplicados = arrayListOf<Int>()
        for (element in ventasDatabaseList){
            arrayIdFacturaDuplicados.add(element.ID_Factura)
        }
        val arrayIdFactura = arrayIdFacturaDuplicados.toSet().toList()


        var ventaList = listOf<Ventas>()
        for (idFactura in arrayIdFactura){
            var name = ""
            var date:Date = Date()
            var price = 0.0f
            var profit = 0.0f
            var id = 0
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            for (elemento in ventasDatabaseList){
                if(idFactura == elemento.ID_Factura) {
                    name += elemento.Name + ","
                    date = formato.parse(elemento.Date)?:Date()
                    price += elemento.Price
                    profit += elemento.Profit
                    id = elemento.ID_Factura
                }
            }

            ventaList += Ventas(date,id,name,price,profit)
        }

        return ventaList
    }


//    companion object{
//        val ventaList = listOf<Ventas>(
//            Ventas(
//                Date(),
//                1,
//                "Camiseta,Pantalon,Pantalon,Pantalon,Pantalon,Pantalon,Pantalon,Pantalon,Pantalon",
//                486.00f,
//                120.00f),
//            Ventas(
//                Date(),
//                2,
//                "Licra,Pantalon",
//                200.00f,
//                120.00f),
//
//        )
//    }
}