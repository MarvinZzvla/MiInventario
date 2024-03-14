package com.vendetta.miinventario.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.vendetta.miinventario.data.database.entities.FinanzasEntity

@Dao
interface FinanzasDao {
    @Insert
    suspend fun insert(finanza:FinanzasEntity)
    @Update
    suspend fun update(finanza:FinanzasEntity)
    @Delete
    suspend fun delete(finanza:FinanzasEntity)
    @Query(value = "SELECT * FROM Finanzas")
    suspend fun getAllFinanzas():List<FinanzasEntity>

    @Query(value = "DELETE FROM Productos")
    suspend fun deleteAll()
}