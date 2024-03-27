package com.vendetta.miinventario.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.vendetta.miinventario.data.database.entities.ProductosEntity

@Dao
interface ProductosDao {
    @Insert
    suspend fun insertAll(producto: ProductosEntity):Long
    @Update
    suspend fun update(producto: ProductosEntity):Int
    @Delete
    suspend fun delete(producto: ProductosEntity)
    @Query(value = "SELECT * FROM Productos")
    suspend fun getAllProductos():List<ProductosEntity>

    @Query(value = "DELETE FROM Productos")
    suspend fun deleteAll()

    @Query(value = "UPDATE Productos SET Available = :cantidad WHERE ID = :id")
    suspend fun updateById(id:Int,cantidad:Int)

    @Query(value = "UPDATE Productos SET Available = Available+:valor WHERE ID = :id")
    suspend fun sumarCantidadById(id: Int, valor:Int)
    @Query(value = "UPDATE Productos SET Available = Available-:valor WHERE ID = :id")
    suspend fun restarCantidadById(id: Int, valor:Int)
}