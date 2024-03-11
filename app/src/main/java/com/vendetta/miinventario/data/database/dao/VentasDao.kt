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
@Query(value = "SELECT * FROM Ventas")
suspend fun getAllVentas():List<VentasEntity>

@Query(value = "DELETE FROM Ventas")
suspend fun deleteAll()
}