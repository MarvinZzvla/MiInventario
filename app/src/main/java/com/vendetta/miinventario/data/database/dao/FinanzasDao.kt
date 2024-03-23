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
   @Query(value = "DELETE FROM Finanzas WHERE FK_Venta = :idFactura")
    suspend fun deleteFinanzas(idFactura:Int):Int

    @Query(value = "SELECT * FROM FINANZAS WHERE Date >= :startDate AND Date <= :endDate")
    suspend fun getFinanzasbyDate(startDate : String,endDate:String):List<FinanzasEntity>

    @Query(value = "SELECT ID,Date,Sum(Total) as Total , Sum(Total_Ganancias) as Total_Ganancias,FK_Venta FROM FINANZAS GROUP BY Date")
    suspend fun getFinanzasbyGroup():List<FinanzasEntity>

}