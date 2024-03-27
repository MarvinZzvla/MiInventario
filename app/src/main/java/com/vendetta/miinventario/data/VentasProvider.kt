package com.vendetta.miinventario.data

import android.content.Context
import com.vendetta.miinventario.data.database.InventarioDatabase
import com.vendetta.miinventario.data.database.InventarioDatabase.Companion.getDatabase
import com.vendetta.miinventario.data.structures.NuevaVentaDatos
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VentasProvider {
    lateinit var database : InventarioDatabase
    suspend fun getAllVentas(context: Context): List<Ventas>{
        database = getDatabase(context)
        //Get all ventas in the database
        val ventasDatabaseList = database.ventasDao.getAllVentas()
        //Create a list to save factura numbers of all productos
        var arrayIdFacturaDuplicados = arrayListOf<Int>()
        for (element in ventasDatabaseList){
            arrayIdFacturaDuplicados.add(element.ID_Factura)
        }
        //Delete duplicates using just 1 factura number
        val arrayIdFactura = arrayIdFacturaDuplicados.toSet().toList()

        //Create a list of Ventas
        var ventaList = listOf<Ventas>()
        //Run all the facturas number
        for (idFactura in arrayIdFactura){
            var name = ""
            var date:Date = Date()
            var price = 0.0f
            var profit = 0.0f
            var id = 0
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            for (elemento in ventasDatabaseList){
                if(idFactura == elemento.ID_Factura) {
                    name += elemento.productos + ","
                    date = formato.parse(elemento.Date)?:Date()
                    price += elemento.Price
                    profit += elemento.Profit
                    id = elemento.ID_Factura
                }
            }

            ventaList += Ventas(date,id,name,price,profit)
        }

        return ventaList.sortedByDescending{it.factura}
    }

    suspend fun getVentabyId(context: Context,id: Int):ArrayList<NuevaVentaDatos>{
        var listaVenta = arrayListOf<NuevaVentaDatos>()
        database = getDatabase(context)
        var listaDatabase = database.ventasDao.getAllVentas()
        for(venta in listaDatabase){
            if(venta.ID_Factura == id) {
                var totalPrice = venta.Price * venta.Quantity
                var totalProfit = venta.Profit * venta.Quantity
                listaVenta.add(
                    NuevaVentaDatos(
                        venta.FK_Producto.toInt(),
                        venta.productos,
                        venta.Quantity,
                        totalPrice,
                        totalProfit,
                        venta.Date
                    )
                )
            }
        }
        return listaVenta
    }

}