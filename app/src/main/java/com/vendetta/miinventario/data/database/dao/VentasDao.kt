package com.vendetta.miinventario.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.vendetta.miinventario.data.Ventas
import com.vendetta.miinventario.data.database.entities.VentasEntity

@Dao
interface VentasDao {
@Insert
suspend fun insertAll(venta:VentasEntity)
@Update
suspend fun update(venta:VentasEntity)
@Delete
suspend fun delete(venta: VentasEntity)
@Query(value = "SELECT * FROM VENTAS ORDER BY ID_Factura")
suspend fun getAllVentas():List<VentasEntity>
@Query(value = "DELETE FROM Ventas")
suspend fun deleteAll()
@Query(value = "DELETE FROM Ventas WHERE ID_Factura = :idFactura")
suspend fun deleteById(idFactura : Int)
@Query(value = "SELECT MAX(ID_Factura) FROM VENTAS")
suspend fun getMaxIdFactura():Int?

}