package com.vendetta.miinventario.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.vendetta.miinventario.data.Ventas
import com.vendetta.miinventario.data.VentasDatabase
import com.vendetta.miinventario.data.database.entities.VentasEntity

@Dao
interface VentasDao {
@Insert
suspend fun insertAll(venta:VentasEntity)
@Update
suspend fun update(venta:VentasEntity)
@Delete
suspend fun delete(venta: VentasEntity)
@Query(value = "SELECT p.Name,v.Date,v.Quantity,v.Price,v.Profit,v.ID_Factura FROM VENTAS v INNER JOIN Productos p ON v.FK_Producto = p.ID ORDER BY v.ID_Factura")
suspend fun getAllVentas():List<VentasDatabase>
@Query(value = "DELETE FROM Ventas")
suspend fun deleteAll()

}